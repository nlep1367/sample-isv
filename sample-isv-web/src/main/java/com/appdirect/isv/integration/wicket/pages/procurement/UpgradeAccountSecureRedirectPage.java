package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.integration.remote.type.EventType;
import com.appdirect.isv.integration.remote.vo.EventInfo;

/**
 * This page is secured and acts as an authorization intermediary for the interactive upgrade subscription flow.
 * The user must first go through this secured page to be authenticated and then will be redirected to the unsecured
 * UpgradeAccountPage to finish the upgrade flow.
 */
@MountPath("/appdirect/upgrade")
public class UpgradeAccountSecureRedirectPage extends BaseIntegrationPage {
	public UpgradeAccountSecureRedirectPage(PageParameters parameters) {
		super(parameters);

		EventInfo eventInfo = readEvent(parameters);
		if (eventInfo == null || eventInfo.getType() != EventType.SUBSCRIPTION_CHANGE) {
			throw new IllegalStateException("Invalid event object.");
		}
		final UpgradeAccountPage upgradeAccountPage = new UpgradeAccountPage(eventInfo, this.applicationProfile, this.oauthUrlSigner);
		setResponsePage(upgradeAccountPage);
	}
}
