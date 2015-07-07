package com.appdirect.isv.integration.service;

import com.appdirect.isv.integration.remote.service.AppDirectIntegrationAPI;
import com.appdirect.isv.integration.remote.vo.APIResult;
import com.appdirect.isv.model.ApplicationProfile;

public interface IntegrationService {
	String SAML_IDP_LINK = "samlIdp";

	AppDirectIntegrationAPI getAppDirectIntegrationApi(String basePath, ApplicationProfile applicationProfile);

	public APIResult processEvent(ApplicationProfile applicationProfile, String eventUrl, String token);
}
