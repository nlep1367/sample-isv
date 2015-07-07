package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.appdirect.isv.integration.oauth.OAuthUrlSigner;
import com.appdirect.isv.integration.oauth.OAuthUrlSignerImpl;
import com.appdirect.isv.integration.remote.service.AppDirectIntegrationAPI;
import com.appdirect.isv.integration.remote.vo.EventInfo;
import com.appdirect.isv.integration.service.IntegrationService;
import com.appdirect.isv.integration.util.IntegrationUtils;
import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.security.oauth.ContextualApplicationProfileGetter;
import com.appdirect.isv.web.wicket.pages.BaseWebPage;
import com.google.common.base.Preconditions;

public abstract class BaseIntegrationPage extends BaseWebPage {
	private static final long serialVersionUID = -6204718580885978058L;

	public static final String EVENT_URL_PARAM = "eventUrl";
	public static final String TOKEN_PARAM = "token";

	@SpringBean
	protected ContextualApplicationProfileGetter contextualApplicationProfileGetter;
	@SpringBean
	private IntegrationService integrationService;

	protected final ApplicationProfile applicationProfile;
	protected final OAuthUrlSigner oauthUrlSigner;
	protected final String basePath;
	protected final String token;

	public BaseIntegrationPage(PageParameters parameters) {
		super(parameters);
		this.applicationProfile = contextualApplicationProfileGetter.get()
				.orElseThrow(() -> new AuthenticationServiceException("Cannot find contextual application profile."));
		this.oauthUrlSigner = new OAuthUrlSignerImpl(applicationProfile.getOauthConsumerKey(), applicationProfile.getOauthConsumerSecret());
		if (applicationProfile.isLegacy()) {
			this.basePath = applicationProfile.getLegacyMarketplaceBaseUrl();
			this.token = parameters.get(TOKEN_PARAM).toOptionalString();
		} else {
			String eventUrl = parameters.get(EVENT_URL_PARAM).toOptionalString();
			this.basePath = IntegrationUtils.extractBasePath(eventUrl);
			this.token = IntegrationUtils.extractToken(eventUrl);
		}
		Preconditions.checkState(StringUtils.isNotBlank(this.basePath), "basePath should not be blank");
	}

	protected EventInfo readEvent(PageParameters parameters) {
		AppDirectIntegrationAPI api = integrationService.getAppDirectIntegrationApi(basePath, applicationProfile);
		EventInfo eventInfo = api.readEvent(token);
		if (!basePath.equals(eventInfo.getMarketplace().getBaseUrl())) {
			throw new IllegalArgumentException("Event partner mismatch.");
		}
		return eventInfo;
	}
}
