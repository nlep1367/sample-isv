package com.appdirect.isv.integration.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.appdirect.isv.integration.remote.service.AppDirectIntegrationAPI;
import com.appdirect.isv.integration.remote.vo.APIResult;

public interface IntegrationService {
	AppDirectIntegrationAPI getAppDirectIntegrationApi(String basePath);

	public APIResult processEvent(@Context HttpServletRequest request, @QueryParam("eventUrl") String eventUrl, @QueryParam("token") String token);
}
