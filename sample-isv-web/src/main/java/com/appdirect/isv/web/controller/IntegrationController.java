package com.appdirect.isv.web.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appdirect.isv.integration.remote.vo.APIResult;
import com.appdirect.isv.integration.service.IntegrationService;

@RestController
@RequestMapping("/api/integration/appdirect")
public class IntegrationController {
	@Autowired
	private IntegrationService integrationService;

	@RequestMapping("/processEvent")
	public APIResult processEvent(HttpServletRequest request, @RequestParam(value = "eventUrl", required = false) String eventUrl, @RequestParam(value = "token", required = false) String token) {
		return integrationService.processEvent(request, eventUrl, token);
	}
}
