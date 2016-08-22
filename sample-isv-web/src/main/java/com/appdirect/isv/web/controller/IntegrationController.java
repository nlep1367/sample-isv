package com.appdirect.isv.web.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.appdirect.isv.integration.remote.vo.APIResult;
import com.appdirect.isv.integration.service.IntegrationService;
import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.security.oauth.ContextualApplicationProfileGetter;
import com.appdirect.isv.web.api.AppDirectIntegrationApi;
import com.appdirect.isv.web.api.AppDirectIntegrationApiFactory;
import com.google.common.base.Preconditions;

@Slf4j
@RestController
@RequestMapping("/api/integration/appdirect")
public class IntegrationController {
	private static final String UNKNOWN = "unknown";
	private static final String X_FORWARDED_FOR = "x-forwarded-for";
	private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
	private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

	@Autowired
	private ContextualApplicationProfileGetter contextualApplicationProfileGetter;
	@Autowired
	private IntegrationService integrationService;
	@Autowired
	private ThreadPoolTaskScheduler threadPoolTaskScheduler;
	@Autowired
	private AppDirectIntegrationApiFactory appDirectIntegrationApiFactory;

	@RequestMapping("/processEvent")
	public APIResult processEvent(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "eventUrl", required = false) String eventUrl,
			@RequestParam(value = "token", required = false) String token,
			@RequestParam(value = "asyncDelay", required = false) Integer asyncDelayMillis) {
		try {
			ApplicationProfile applicationProfile = contextualApplicationProfileGetter.get().orElseThrow(() -> new AuthenticationServiceException("Contextual application profile not found"));
			Preconditions.checkState(asyncDelayMillis == null || eventUrl != null, "eventUrl must be specified if asyncDelay is specified"); // async doesn't work with just a token
			APIResult apiResult = processEvent(applicationProfile, extractIpAddress(request), eventUrl, token);
			if (asyncDelayMillis != null) {
				apiResult = registerResultAfterAsyncDelay(applicationProfile, eventUrl, apiResult, asyncDelayMillis, response);
			}
			return apiResult;
		} catch (RuntimeException e) {
			log.error("Error processing event with eventUrl={} or token={}", eventUrl, token, e);
			return toAPIResult(e);
		}
	}

	private APIResult processEvent(ApplicationProfile applicationProfile, String clientIpAddress, String eventUrl, String token) {
		APIResult apiResult = integrationService.processEvent(applicationProfile, eventUrl, token);
		apiResult.setMessage(String.format("From IP: %s. %s", clientIpAddress, apiResult.getMessage()));
		log.info("Result of processing event with eventUrl={} or token={}: {}", eventUrl, token, apiResult);
		return apiResult;
	}

	private APIResult registerResultAfterAsyncDelay(ApplicationProfile applicationProfile, String eventUrl, APIResult apiResult, int asyncDelayMillis, HttpServletResponse response) {
		response.setStatus(HttpStatus.ACCEPTED.value());
		Date startTime = DateUtils.addMilliseconds(new Date(), asyncDelayMillis);
		log.info("Result for event with eventUrl={} will be registered asynchronously in {} milliseconds", eventUrl, asyncDelayMillis);
		threadPoolTaskScheduler.schedule(() -> registerResult(applicationProfile, eventUrl, apiResult), startTime);
		return new APIResult(true, String.format("Event result will be registered asynchronously in %s milliseconds.", asyncDelayMillis));
	}

	private void registerResult(ApplicationProfile applicationProfile, String eventUrl, APIResult apiResult) {
		try {
			AppDirectIntegrationApi appDirectIntegrationApi = appDirectIntegrationApiFactory.get(applicationProfile);
			appDirectIntegrationApi.registerResult(eventUrl, apiResult);
		} catch (RuntimeException e) {
			log.error("Error registering asynchronous event result", e);
		}
	}

	private APIResult toAPIResult(RuntimeException e) {
		APIResult result;
		result = new APIResult();
		result.setSuccess(false);
		result.setErrorCode(ErrorCode.UNKNOWN_ERROR);
		StringBuilder message = new StringBuilder(e.getMessage() != null ? e.getMessage() : e.toString()).append("\n");
		int i = 0;
		for (StackTraceElement element : e.getStackTrace()) {
			message.append(element.toString()).append("\n");
			if (i++ > 5) {
				break;
			}
		}
		result.setMessage(message.toString());
		return result;
	}

	private String extractIpAddress(HttpServletRequest request) {
		String ip = request.getHeader(X_FORWARDED_FOR);
		if (StringUtils.isBlank(ip) || ip.equalsIgnoreCase(UNKNOWN)) {
			ip = request.getHeader(HTTP_CLIENT_IP);
		}
		if (StringUtils.isBlank(ip) || ip.equalsIgnoreCase(UNKNOWN)) {
			ip = request.getHeader(HTTP_X_FORWARDED_FOR);
		}
		if (StringUtils.isBlank(ip) || ip.equalsIgnoreCase(UNKNOWN)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
}
