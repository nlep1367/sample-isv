package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.integration.remote.type.EventType;
import com.appdirect.isv.integration.remote.vo.EventInfo;

/**
 * This page is secured and acts as an authorization intermediary for the interactive cancel subscription flow.
 * The user must first go through this secured page to be authenticated and then will be redirected to the unsecured
 * CancelAccountPage to finish the cancellation flow.
 */
@MountPath("/appdirect/cancel")
public class CancelAccountSecureRedirectPage extends BaseIntegrationPage {

	public CancelAccountSecureRedirectPage(final PageParameters parameters) {
		super(parameters);

		EventInfo eventInfo = readEvent(parameters);
		if (eventInfo == null || eventInfo.getType() != EventType.SUBSCRIPTION_CANCEL) {
			throw new IllegalStateException("Invalid event object.");
		}
		final CancelAccountPage cancelAccountPage = new CancelAccountPage(eventInfo, this.oauthUrlSigner);
		setResponsePage(cancelAccountPage);
	}
}
