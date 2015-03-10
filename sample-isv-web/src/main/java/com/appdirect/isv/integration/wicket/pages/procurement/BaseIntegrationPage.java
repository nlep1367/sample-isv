package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.appdirect.isv.config.ServerConfiguration;
import com.appdirect.isv.integration.remote.service.AppDirectIntegrationAPI;
import com.appdirect.isv.integration.remote.vo.EventInfo;
import com.appdirect.isv.integration.service.IntegrationService;
import com.appdirect.isv.integration.util.IntegrationUtils;
import com.appdirect.isv.web.wicket.pages.BaseWebPage;

public abstract class BaseIntegrationPage extends BaseWebPage {
	private static final long serialVersionUID = -6204718580885978058L;

	public static final String EVENT_URL_PARAM = "eventUrl";
	public static final String TOKEN_PARAM = "token";

	@SpringBean
	private ServerConfiguration serverConfiguration;
	@SpringBean
	private IntegrationService integrationService;

	public BaseIntegrationPage() {
		super();
	}

	public BaseIntegrationPage(PageParameters parameters) {
		super(parameters);
	}

	protected String getBasePath(PageParameters parameters) {
		String basePath = serverConfiguration.getAppDirectBaseUrl();
		String eventUrl = parameters.get(EVENT_URL_PARAM).toOptionalString();
		if (StringUtils.isNotBlank(eventUrl)) {
			basePath = IntegrationUtils.extractBasePath(eventUrl);
		}
		return basePath;
	}

	protected EventInfo readEvent(PageParameters parameters) {
		String basePath = getBasePath(parameters);

		String token = parameters.get(TOKEN_PARAM).toOptionalString();
		String eventUrl = parameters.get(EVENT_URL_PARAM).toOptionalString();
		if (StringUtils.isNotBlank(eventUrl)) {
			token = IntegrationUtils.extractToken(eventUrl);
		}

		AppDirectIntegrationAPI api = integrationService.getAppDirectIntegrationApi(basePath);
		EventInfo eventInfo = api.readEvent(token);
		if (StringUtils.isNotBlank(eventUrl) && !basePath.equals(eventInfo.getMarketplace().getBaseUrl())) {
			// API 1.1 and event comes from an untrusted source.
			throw new IllegalArgumentException("Event partner mismatch.");
		}
		return eventInfo;
	}
}
