package com.appdirect.isv.web.api;

import com.appdirect.isv.integration.remote.vo.APIResult;

@FunctionalInterface
public interface AppDirectIntegrationApi {
	void registerResult(String eventToken, APIResult apiResult);
}
