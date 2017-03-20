package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.ObjectNotFoundException;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.integration.oauth.OAuthUrlSigner;
import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.appdirect.isv.integration.remote.vo.EventInfo;
import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.BaseWebPage;

/**
 * The Upgrade Account Page cannot be access directly.
 * The user must first go through the secure version of this page, which is the UpgradeAccountSecureRedirectPage, and
 * then be redirected to this unsecured page to finish the interactive subscription upgrade flow.
 */
@MountPath("/unsecure/appdirect/upgrade")
public class UpgradeAccountPage extends BaseWebPage {
	private static final long serialVersionUID = 1193297070006588092L;

	@SpringBean
	private AccountService accountService;

	public UpgradeAccountPage(EventInfo eventInfo, ApplicationProfile applicationProfile, OAuthUrlSigner oauthUrlSigner) {
		StringBuilder returnUrl = new StringBuilder(eventInfo.getReturnUrl());

		add(new Label("accountIdentifier", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		add(new Label("openId", eventInfo.getCreator().getOpenId()));
		add(new Label("firstName", eventInfo.getCreator().getFirstName()));
		add(new Label("lastName", eventInfo.getCreator().getLastName()));
		add(new Label("editionCode", eventInfo.getPayload().getOrder().getEditionCode()));
		add(new Label("numOfSeats", String.valueOf(eventInfo.getPayload().getOrder().getMaxUsers())));
		add(new Label("returnUrl", eventInfo.getReturnUrl()));
		add(new Link<Void>("upgradeAccount") {
			private static final long serialVersionUID = 3944839969882170140L;

			@Override
			public void onClick() {
				Long accountId = Long.valueOf(eventInfo.getPayload().getAccount().getAccountIdentifier());
 				AccountBean accountBean = new AccountBean(applicationProfile);
 				accountBean.setId(accountId);
				accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
				accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
				try {
					accountService.updateAccount(accountBean);
					returnUrl.append("&success=true");
				} catch (ObjectNotFoundException onfe) {
					returnUrl.append("&success=false&errorCode=").append(ErrorCode.ACCOUNT_NOT_FOUND.toString());
				}
				String signedUrl = oauthUrlSigner.sign(returnUrl.toString());
				throw new RedirectToUrlException(signedUrl);
			}
		});

		add(new Link<Void>("failure") {
			private static final long serialVersionUID = 3944839969882170140L;

			@Override
			public void onClick() {
				String failureUrl = returnUrl.append("&success=false&errorCode=")
						.append(ErrorCode.UNKNOWN_ERROR)
						.append("&message=Simulating+a+subscription+upgrade+failure")
						.toString();
				String signedUrl = oauthUrlSigner.sign(failureUrl);
				throw new RedirectToUrlException(signedUrl);
			}
		});
	}
}
