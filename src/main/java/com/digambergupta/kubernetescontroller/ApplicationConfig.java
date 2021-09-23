package com.digambergupta.kubernetescontroller;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.digambergupta.kubernetescontroller.reconclier.DeploymentCounterReconciler;

import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.builder.DefaultControllerBuilder;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.GenericKubernetesApi;

@Configuration
public class ApplicationConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

	@Bean
	public ApiClient kubeClient() throws IOException {
		return Config.defaultClient();
	}

	@Bean
	public SharedInformerFactory sharedInformerFactory() {
		return new SharedInformerFactory();
	}

	@Bean
	public CommandLineRunner commandLineRunner(final SharedInformerFactory sharedInformerFactory, final Controller deploymentCounterController) {
		return args -> {
			LOGGER.info("starting informers..");
			sharedInformerFactory.startAllRegisteredInformers();

			LOGGER.info("running controller..");
			deploymentCounterController.run();
		};
	}

	@Bean
	public Controller deploymentCounterController(final SharedInformerFactory sharedInformerFactory, final DeploymentCounterReconciler reconciler) {
		DefaultControllerBuilder builder = ControllerBuilder.defaultBuilder(sharedInformerFactory);
		builder =
				builder.watch(
						(q) -> ControllerBuilder.controllerWatchBuilder(V1Deployment.class, q)
								.withResyncPeriod(Duration.ofMinutes(1))
								.build());
		builder.withWorkerCount(2);
		builder.withReadyFunc(reconciler::informerReady);
		return builder.withReconciler(reconciler).withName("deploymentCounterController").build();
	}

	@Bean
	public SharedIndexInformer<V1Deployment> deploymentInformer(ApiClient apiClient, SharedInformerFactory sharedInformerFactory) {
		final GenericKubernetesApi<V1Deployment, V1DeploymentList> genericApi =
				new GenericKubernetesApi<>(V1Deployment.class, V1DeploymentList.class, "apps", "v1", "deployments", apiClient);
		return sharedInformerFactory.sharedIndexInformerFor(genericApi, V1Deployment.class, 0);
	}

}
