package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.ObjectNotFoundException;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.integration.oauth.OAuthUrlSigner;
import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.appdirect.isv.integration.remote.vo.EventInfo;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.BaseWebPage;

/**
 * The Cancel Account Page cannot be access directly.
 * The user must first go through the secure version of this page, which is the CancelAccountSecureRedirectPage, and
 * then be redirected to this unsecured page to finish the interactive subscription cancellation flow.
 */
@MountPath("/unsecure/appdirect/cancel")
public class CancelAccountPage extends BaseWebPage {
	private static final long serialVersionUID = -1432964208174602765L;

	@SpringBean
	private AccountService accountService;

	public CancelAccountPage(EventInfo eventInfo, OAuthUrlSigner oauthUrlSigner) {
		StringBuilder returnUrl = new StringBuilder(eventInfo.getReturnUrl());

		add(new Label("openId", eventInfo.getCreator().getOpenId()));
		add(new Label("firstName", eventInfo.getCreator().getFirstName()));
		add(new Label("lastName", eventInfo.getCreator().getLastName()));
		add(new Label("accountIdentifier", eventInfo.getPayload().getAccount().getAccountIdentifier()));
		add(new Label("returnUrl", eventInfo.getReturnUrl()));
		add(new Link<Void>("cancelAccount") {
			private static final long serialVersionUID = -9095275432565219418L;

			@Override
			public void onClick() {
				// Delete the account.
				try {
					Long accountId = Long.valueOf(eventInfo.getPayload().getAccount().getAccountIdentifier());
					accountService.deleteAccount(accountId);
					returnUrl.append("&success=true");
				} catch (ObjectNotFoundException | NumberFormatException e) {
					returnUrl.append("&success=false&errorCode=").append(ErrorCode.ACCOUNT_NOT_FOUND);
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
						.append("&message=Simulating+a+subscription+cancellation+failure")
						.toString();
				String signedUrl = oauthUrlSigner.sign(failureUrl);
				throw new RedirectToUrlException(signedUrl);
			}
		});
	}
}
