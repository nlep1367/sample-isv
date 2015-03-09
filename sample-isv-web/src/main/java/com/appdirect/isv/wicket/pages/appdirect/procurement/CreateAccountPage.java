package com.appdirect.isv.wicket.pages.appdirect.procurement;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.backend.integration.remote.type.ErrorCode;
import com.appdirect.isv.backend.integration.remote.type.EventType;
import com.appdirect.isv.backend.integration.remote.vo.EventInfo;
import com.appdirect.isv.backend.user.vo.AccountBean;
import com.appdirect.isv.backend.user.vo.UserBean;
import com.appdirect.isv.wicket.application.ISVApplication;

@MountPath("appdirect/create")
public class CreateAccountPage extends BaseIntegrationPage {
	private static final long serialVersionUID = 7526823547605447478L;

	public CreateAccountPage(final PageParameters parameters) {
		super(parameters);

		final EventInfo eventInfo = readEvent(parameters);

		if (eventInfo == null || eventInfo.getType() != EventType.SUBSCRIPTION_ORDER) {
			throw new IllegalStateException("Invalid event object.");
		}

		if (ISVApplication.get().getIsvService().readUserByOpenID(eventInfo.getCreator().getOpenId()) != null) {
			try {
				String errorUrl = eventInfo.getReturnUrl() + "&success=false&errorCode=" + ErrorCode.USER_ALREADY_EXISTS + "&message=" + URLEncoder.encode("An account with this user already exists. Use the Import Account", "UTF-8");
				String signedUrl = ISVApplication.get().getOauthUrlSigner().sign(errorUrl);
				throw new RedirectToUrlException(signedUrl);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
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
		add(new Link<Void>("createAccount") {
			private static final long serialVersionUID = 3944839969882170140L;

			@Override
			public void onClick() {
				// Create the account.
				UserBean adminBean = new UserBean();
				adminBean.setUuid(eventInfo.getCreator().getUuid());
				adminBean.setOpenId(eventInfo.getCreator().getOpenId());
				adminBean.setEmail(eventInfo.getCreator().getEmail());
				adminBean.setFirstName(eventInfo.getCreator().getFirstName());
				adminBean.setLastName(eventInfo.getCreator().getLastName());
				adminBean.setAdmin(true);

				AccountBean accountBean = new AccountBean();
				accountBean.setUuid(eventInfo.getPayload().getCompany().getUuid());
				accountBean.setName(accountBean.getUuid());
				accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
				accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
				accountBean.setAppDirectManaged(true);
				accountBean.setAppDirectBaseUrl(getBasePath(parameters));

				ISVApplication.get().getIsvService().createAccount(accountBean, adminBean);
				String successUrl = eventInfo.getReturnUrl() + "&success=true&accountIdentifier=" + accountBean.getUuid();
				String signedUrl = ISVApplication.get().getOauthUrlSigner().sign(successUrl);
				throw new RedirectToUrlException(signedUrl);
			}
		});
	}
}
