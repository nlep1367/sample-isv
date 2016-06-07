package com.appdirect.isv.web.api;

import org.springframework.stereotype.Component;

import com.appdirect.isv.model.ApplicationProfile;

@Component
public class AppDirectIntegrationApiFactory {
	public AppDirectIntegrationApi get(ApplicationProfile applicationProfile) {
		return new AppDirectIntegrationApiImpl(applicationProfile);
	}
}
