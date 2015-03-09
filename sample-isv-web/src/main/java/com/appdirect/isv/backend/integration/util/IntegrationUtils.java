package com.appdirect.isv.backend.integration.util;

import org.apache.commons.lang3.StringUtils;

public final class IntegrationUtils {
	private IntegrationUtils() {
		// Don't new me.
	}

	public static String extractBasePath(String eventUrl) {
		int index = eventUrl.lastIndexOf("/api/integration/v1");
		return StringUtils.substring(eventUrl, 0, index);
	}

	public static String extractToken(String eventUrl) {
		String[] path = eventUrl.split("/");
		return path[path.length - 1];
	}
}
