package com.appdirect.isv.web.api;

import org.springframework.web.client.RestTemplate;

import com.appdirect.isv.integration.remote.vo.APIResult;
import com.appdirect.isv.model.ApplicationProfile;

public class AppDirectIntegrationApiImpl implements AppDirectIntegrationApi {
	private final RestTemplate restTemplate;

	public AppDirectIntegrationApiImpl(ApplicationProfile applicationProfile) {
		restTemplate = new RestTemplate(new TwoLeggedOAuthClientHttpRequestFactory(applicationProfile));
	}

	@Override
	public void registerResult(String eventUrl, APIResult apiResult) {
		restTemplate.postForObject(eventUrl + "/result", apiResult, Void.class);
	}
}
