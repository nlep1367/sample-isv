package com.appdirect.isv.integration.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appdirect.isv.integration.remote.service.AppDirectIntegrationAPI;
import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.appdirect.isv.integration.remote.vo.APIResult;

@Service
public class IntegrationServiceImpl implements IntegrationService {
	private static final String UNKNOWN = "unknown";
	private static final String X_FORWARDED_FOR = "x-forwarded-for";
	private static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
	private static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";

	@Autowired
	private IntegrationHelper integrationHelper;

	@Override
	public AppDirectIntegrationAPI getAppDirectIntegrationApi(String basePath) {
		return integrationHelper.getAppDirectIntegrationApi(basePath);
	}

	@Override
	public APIResult processEvent(HttpServletRequest request, String eventUrl, String token) {
		APIResult result;
		try {
			result = integrationHelper.processEvent(eventUrl, token);
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
