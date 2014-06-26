package com.appdirect.isv.wicket.pages.authentication;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDConsumerException;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.wicket.application.ISVApplication;
import com.appdirect.isv.wicket.session.ISVSession;
import com.appdirect.isv.wicket.util.RedirectToPageException;

@Slf4j
@MountPath(OpenIDReturnPage.MOUNT_PATH)
public class OpenIDReturnPage extends WebPage {
	private static final long serialVersionUID = -4561114616009174561L;

	public static final String MOUNT_PATH = "openid/finish";

	public OpenIDReturnPage(PageParameters parameters) {
		super(parameters);
		OpenIDAuthenticationToken token = null;
		try {
			token = ISVApplication.get().getOpenIdConsumer().endConsumption((HttpServletRequest) getRequest().getContainerRequest());
		} catch (OpenIDConsumerException e) {
			log.error("Error during OpenID end", e);
			getSession().error(e.getMessage());
		}
		if (token != null && token.getStatus() == OpenIDAuthenticationStatus.SUCCESS) {
				log.info("OpenID login for {}.", token.getIdentityUrl());
				try {
					if (ISVSession.get().authenticate(token)) {
						getSession().info("Welcome back!");
						continueToOriginalDestination();
						throw new RedirectToPageException(ISVApplication.get().getHomePage());
					}
				} catch (UsernameNotFoundException e) {
					getSession().error(String.format("There is no user with OpenID %s", token.getIdentityUrl()));
				} catch (AuthenticationException e) {
					log.error("An error occurred during authentication", e);
					getSession().error("The authentication failed.");
				}
				throw new RedirectToPageException(ISVApplication.get().getSignInPageClass());
		} else {
			getSession().error("The OpenID process did not complete successfully. Please try again.");
			throw new RedirectToPageException(ISVApplication.get().getSignInPageClass());
		}
	}
}
