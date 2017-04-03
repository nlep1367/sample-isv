package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.integration.remote.type.EventType;
import com.appdirect.isv.integration.remote.vo.EventInfo;

/**
 * This page is secured and acts as an authorization intermediary for the interactive create subscription flow.
 * The user must first go through this secured page to be authenticated and then will be redirected to the unsecured
 * CreateAccountPage to finish the creation flow.
 */
@MountPath("/appdirect/create")
public class CreateAccountSecureRedirectPage extends BaseIntegrationPage {

	public CreateAccountSecureRedirectPage(final PageParameters parameters) {
		super(parameters);

		EventInfo eventInfo = readEvent(parameters);
		if (eventInfo == null || eventInfo.getType() != EventType.SUBSCRIPTION_ORDER) {
			throw new IllegalStateException("Invalid event object.");
		}
		final CreateAccountPage createAccountPage = new CreateAccountPage(eventInfo, this.basePath, this.applicationProfile, this.oauthUrlSigner);
		setResponsePage(createAccountPage);
	}
}
