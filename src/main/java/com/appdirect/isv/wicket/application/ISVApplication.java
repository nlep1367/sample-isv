package com.appdirect.isv.wicket.application;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.stereotype.Component;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

import com.appdirect.isv.backend.core.util.ServerConfiguration;
import com.appdirect.isv.backend.integration.service.IntegrationService;
import com.appdirect.isv.backend.security.oauth.OAuthUrlSigner;
import com.appdirect.isv.backend.user.service.ISVService;
import com.appdirect.isv.wicket.pages.HomePage;
import com.appdirect.isv.wicket.pages.authentication.LoginPage;
import com.appdirect.isv.wicket.session.ISVSession;

/**
 * Application object for your web application.
 */
@Component
public class ISVApplication extends AuthenticatedWebApplication {
	@Override
	protected void init() {
		super.init();
		new AnnotatedMountScanner().scanPackage("com.appdirect.isv").mount(this);
		getRequestCycleSettings().setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);
	}

	public static ISVApplication get() {
		return (ISVApplication) Application.get();
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

	@Override
	public Class<? extends WebPage> getSignInPageClass() {
		return LoginPage.class;
	}

	@Override
	protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
		return ISVSession.class;
	}

	/*
	 * Spring Beans
	 */
	protected OpenIDConsumer openIdConsumer;
	protected AuthenticationManager authenticationManager;
	protected ISVService isvService;
	protected IntegrationService integrationService;
	protected ServerConfiguration serverConfiguration;
	protected OAuthUrlSigner oauthUrlSigner;

	public OpenIDConsumer getOpenIdConsumer() {
		return openIdConsumer;
	}

	@Autowired
	public void setOpenIdConsumer(OpenIDConsumer openIdConsumer) {
		this.openIdConsumer = openIdConsumer;
	}

	public AuthenticationManager getAuthenticationManager() {
		return authenticationManager;
	}

	@Autowired
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	public ISVService getIsvService() {
		return isvService;
	}

	@Autowired
	public void setIsvService(ISVService isvService) {
		this.isvService = isvService;
	}

	public IntegrationService getIntegrationService() {
		return integrationService;
	}

	@Autowired
	public void setIntegrationService(IntegrationService integrationService) {
		this.integrationService = integrationService;
	}

	public ServerConfiguration getServerConfiguration() {
		return serverConfiguration;
	}

	@Autowired
	public void setServerConfiguration(ServerConfiguration serverConfiguration) {
		this.serverConfiguration = serverConfiguration;
	}

	public OAuthUrlSigner getOauthUrlSigner() {
		return oauthUrlSigner;
	}

	@Autowired
	public void setOauthUrlSigner(OAuthUrlSigner oauthUrlSigner) {
		this.oauthUrlSigner = oauthUrlSigner;
	}
}