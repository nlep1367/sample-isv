package com.appdirect.isv.web.controller;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.appdirect.isv.integration.remote.vo.APIResult;
import com.appdirect.isv.integration.service.IntegrationService;

@Slf4j
@RestController
@RequestMapping("/api/integration/appdirect")
public class IntegrationController {
	private static final String UNKNOWN = "unknown";
	private static final String X_FORWARDED_FOR = "x-forwarded-for";
	private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
	private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

	@Autowired
	private IntegrationService integrationService;

	@RequestMapping("/processEvent")
	public APIResult processEvent(HttpServletRequest request, @RequestParam(value = "eventUrl", required = false) String eventUrl, @RequestParam(value = "token", required = false) String token) {
		APIResult result;
		try {
			result = integrationService.processEvent(eventUrl, token);
		} catch (RuntimeException e) {
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
		}
		result.setMessage(String.format("From IP: %s. %s", extractIpAddress(request), result.getMessage()));
		log.info("Returning [result={}].", result);
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
