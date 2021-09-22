package com.digambergupta.kubernetescontroller.reconclier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import com.digambergupta.counterservice.clients.counter.controller.KubernetesCounterApi;
import com.digambergupta.counterservice.clients.counter.model.ClientKubernetesCounterResponse;
import com.digambergupta.counterservice.clients.counter.model.ClientSubmitKubernetesCounterIncrementRequest;
import com.digambergupta.kubernetescontroller.model.DeploymentInfo;

import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.models.V1Deployment;

@Component
public class DeploymentCounterReconciler implements Reconciler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentCounterReconciler.class);

	private final Lister<V1Deployment> deploymentLister;

	private final SharedIndexInformer<V1Deployment> deploymentInformer;

	private final KubernetesCounterApi counterApi;

	private final String filterByMetadataKey;

	private final String filterByMetadataValue;

	public DeploymentCounterReconciler(SharedIndexInformer<V1Deployment> deploymentInformer,
			KubernetesCounterApi counterApi, @Value("${controller.filter.by.metadata.key}") final String filterByMetadataKey,
			@Value("${controller.filter.by.metadata.value}") final String filterByMetadataValue) {
		this.deploymentLister = new Lister<>(deploymentInformer.getIndexer());
		this.deploymentInformer = deploymentInformer;
		this.counterApi = counterApi;
		this.filterByMetadataKey = filterByMetadataKey;
		this.filterByMetadataValue = filterByMetadataValue;
	}

	// *OPTIONAL*
	// If you want to hold the controller from running util some condition..
	public boolean informerReady() {
		return deploymentInformer.hasSynced();
	}

	@Override
	public Result reconcile(Request request) {
		final List<V1Deployment> list = this.deploymentLister.list();
		final Map<DeploymentInfo, Integer> deploymentInfoMap = new HashMap<>();

		list.stream()
				.filter(v1Deployment -> v1Deployment.getMetadata() != null && Objects.requireNonNull(
						v1Deployment.getMetadata().getLabels()).get(filterByMetadataKey) != null && v1Deployment.getMetadata().getLabels().get(
						filterByMetadataKey).equals(filterByMetadataValue))
				.forEach(v1Deployment -> {
					final DeploymentInfo key = new DeploymentInfo(v1Deployment.getKind(), v1Deployment.getMetadata().getName());
					Integer count = deploymentInfoMap.containsKey(key) ? deploymentInfoMap.get(key) + 1 : 1;
					deploymentInfoMap.put(key, count);
				});

		for (DeploymentInfo deployment : deploymentInfoMap.keySet()) {
			var count = deploymentInfoMap.get(deployment);

			if (count > 0) {
				createOrUpdateCount(deployment.getKind(), deployment.getMetadataName(), count);
			}

			LOGGER.info("Total Number of count for kind: " + deployment.getKind() + " metaDataName: " + deployment.getMetadataName() + " is " + count);
		}

		return new Result(false);
	}

	private int createOrUpdateCount(String kind, String metadataName, Integer count) {
		try {
			final ClientKubernetesCounterResponse kubernetesCounter = counterApi
					.createOrIncrementKubeCounter(new ClientSubmitKubernetesCounterIncrementRequest()
							.kind(kind)
							.metadataName(metadataName)
							.incrementCountBy(count));

			return kubernetesCounter != null && kubernetesCounter.getCount() != null ? kubernetesCounter.getCount() : 0;
		} catch (HttpClientErrorException exception) {
			if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
				return 0;
			}
			LOGGER.error("Counter service client error", exception);
		} catch (Exception exception) {
			LOGGER.error("Unexpected error while calling counter service", exception);
			throw exception;
		}
		return 0;
	}

}