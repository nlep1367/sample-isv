package com.appdirect.isv.wicket.application;

import lombok.Getter;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
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
		// Allow wicket to inject spring beans into components using @SpringBean annotation
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));
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
	@Getter
	@Autowired
	private OpenIDConsumer openIdConsumer;

	@Getter
	@Autowired
	private AuthenticationManager authenticationManager;

	@Getter
	@Autowired
	private ISVService isvService;

	@Getter
	@Autowired
	private IntegrationService integrationService;

	@Getter
	@Autowired
	private ServerConfiguration serverConfiguration;

	@Getter
	@Autowired
	private OAuthUrlSigner oauthUrlSigner;
}