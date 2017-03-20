package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.integration.oauth.OAuthUrlSigner;
import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.appdirect.isv.integration.remote.vo.EventInfo;
import com.appdirect.isv.integration.service.IntegrationService;
import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.BaseWebPage;

/**
 * The Create Account Page cannot be access directly.
 * The user must first go through the secure version of this page, which is the CreateAccountSecureRedirectPage, and
 * then be redirected to this unsecured page to finish the interactive subscription creation flow.
 */
@MountPath("/unsecure/appdirect/create")
public class CreateAccountPage extends BaseWebPage {
	private static final long serialVersionUID = 7526823547605447478L;

	@SpringBean
	private AccountService accountService;

	public CreateAccountPage(EventInfo eventInfo, String basePath, ApplicationProfile applicationProfile, OAuthUrlSigner oauthUrlSigner) {
		StringBuilder returnUrl = new StringBuilder(eventInfo.getReturnUrl());

		add(new Label("uuid", eventInfo.getCreator().getUuid()));
		add(new Label("openId", eventInfo.getCreator().getOpenId()));
		add(new Label("email", eventInfo.getCreator().getEmail()));
		add(new Label("firstName", eventInfo.getCreator().getFirstName()));
		add(new Label("lastName", eventInfo.getCreator().getLastName()));
		add(new Label("companyName", eventInfo.getPayload().getCompany().getName()));
		add(new Label("companyEmail", eventInfo.getPayload().getCompany().getEmail()));
		add(new Label("companyPhone", eventInfo.getPayload().getCompany().getPhoneNumber()));
		add(new Label("companyWebsite", eventInfo.getPayload().getCompany().getWebsite()));
		add(new Label("editionCode", eventInfo.getPayload().getOrder().getEditionCode()));
		add(new Label("numOfSeats", String.valueOf(eventInfo.getPayload().getOrder().getMaxUsers())));
		add(new Label("returnUrl", eventInfo.getReturnUrl()));
		String samlIdpMetadataUrl = eventInfo.hasLink(IntegrationService.SAML_IDP_LINK) ? eventInfo.getLink(IntegrationService.SAML_IDP_LINK).getHref() : null;
		add(new Label("samlIdpMetadataUrl", samlIdpMetadataUrl));
		add(new Link<Void>("createAccount") {
			private static final long serialVersionUID = 3944839969882170140L;

			@Override
			public void onClick() {
				// Create the account.
				UserBean adminBean = new UserBean();
				adminBean.setAppDirectUuid(eventInfo.getCreator().getUuid());
				adminBean.setAppDirectOpenId(eventInfo.getCreator().getOpenId());
				adminBean.setEmail(eventInfo.getCreator().getEmail());
				adminBean.setFirstName(eventInfo.getCreator().getFirstName());
				adminBean.setLastName(eventInfo.getCreator().getLastName());
				adminBean.setAdmin(true);

				AccountBean accountBean = new AccountBean(applicationProfile);
				accountBean.setAppDirectUuid(eventInfo.getPayload().getCompany().getUuid());
				accountBean.setAppDirectBaseUrl(basePath);
				accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
				accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());

				accountService.createAccount(accountBean, adminBean);
				String successUrl = returnUrl.append("&success=true&accountIdentifier=")
						.append(accountBean.getId())
						.toString();
				String signedUrl = oauthUrlSigner.sign(successUrl);
				throw new RedirectToUrlException(signedUrl);
			}
		});
		add(new Link<Void>("failure") {
			private static final long serialVersionUID = 3944839969882170140L;

			@Override
			public void onClick() {
				String failureUrl = returnUrl.append("&success=false&errorCode=")
						.append(ErrorCode.UNKNOWN_ERROR)
						.append("&message=Simulating+a+subscription+creation+failure")
						.toString();
				String signedUrl = oauthUrlSigner.sign(failureUrl);
				throw new RedirectToUrlException(signedUrl);
			}
		});
	}
}
