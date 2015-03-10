package com.appdirect.isv.web.wicket.application;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.springframework.stereotype.Component;
import org.wicketstuff.annotation.scan.AnnotatedMountScanner;

import com.appdirect.isv.web.wicket.pages.HomePage;
import com.appdirect.isv.web.wicket.pages.authentication.LoginPage;
import com.appdirect.isv.web.wicket.session.WicketSession;

/**
 * Application object for your web application.
 */
@Component
public class WicketApplication extends AuthenticatedWebApplication {
	@Override
	protected void init() {
		super.init();
		// Allow wicket to inject spring beans into components using @SpringBean annotation
		getComponentInstantiationListeners().add(new SpringComponentInjector(this));
		new AnnotatedMountScanner().scanPackage("com.appdirect.isv").mount(this);
		getRequestCycleSettings().setRenderStrategy(RenderStrategy.ONE_PASS_RENDER);
	}

	public static WicketApplication get() {
		return (WicketApplication) Application.get();
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
		return WicketSession.class;
	}
}