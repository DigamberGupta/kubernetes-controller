package com.digambergupta.kubernetescontroller;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.digambergupta.counterservice.clients.counter.ApiClient;
import com.digambergupta.counterservice.clients.counter.controller.KubernetesCounterApi;
import com.digambergupta.kubernetescontroller.model.ClientConfiguration;

@Configuration
public class CounterServiceClientConfig {

	@Bean
	@ConfigurationProperties(prefix = "counter-service-client", ignoreUnknownFields = false)
	public ClientConfiguration counterServiceClientConfiguration() {
		return new ClientConfiguration();
	}

	@Bean
	public ApiClient counterServiceClient(RestTemplateBuilder restTemplateBuilder) {
		ClientConfiguration config = counterServiceClientConfiguration();

		RestTemplate restTemplate = restTemplateBuilder //
				.setReadTimeout(config.getReadTimeout()) //
				.setConnectTimeout(config.getConnectionTimeout()) //
				.build();

		ApiClient apiClient = new ApiClient(restTemplate);
		apiClient.setBasePath(config.getBaseUrl());
		apiClient.setApiKey(config.getApiKey());

		return apiClient;
	}

	@Bean
	public KubernetesCounterApi kubernetesCounterClientAPI(RestTemplateBuilder restTemplateBuilder) {
		return new KubernetesCounterApi(counterServiceClient(restTemplateBuilder));
	}

}
