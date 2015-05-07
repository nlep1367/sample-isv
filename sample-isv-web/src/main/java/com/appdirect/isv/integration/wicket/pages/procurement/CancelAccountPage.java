package com.appdirect.isv.integration.wicket.pages.procurement;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.ObjectNotFoundException;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.integration.oauth.OAuthUrlSigner;
import com.appdirect.isv.integration.remote.type.ErrorCode;
import com.appdirect.isv.integration.remote.type.EventType;
import com.appdirect.isv.integration.remote.vo.EventInfo;
import com.appdirect.isv.service.AccountService;

@MountPath("/appdirect/cancel")
public class CancelAccountPage extends BaseIntegrationPage {
	private static final long serialVersionUID = -1432964208174602765L;

	@SpringBean
	private AccountService accountService;
	@SpringBean
	private OAuthUrlSigner oauthUrlSigner;

	public CancelAccountPage(PageParameters parameters) {
		super(parameters);

		EventInfo eventInfo = readEvent(parameters);
		if (eventInfo == null || eventInfo.getType() != EventType.SUBSCRIPTION_CANCEL) {
			throw new IllegalStateException("Invalid event object.");
		}

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
				StringBuilder returnUrl = new StringBuilder(eventInfo.getReturnUrl());
				try {
					AccountBean accountBean = new AccountBean();
					accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
					accountService.delete(accountBean);
					returnUrl.append("&success=true");
				} catch (ObjectNotFoundException e) {
					returnUrl.append("&success=false&errorCode=").append(ErrorCode.ACCOUNT_NOT_FOUND);
				}
				String signedUrl = oauthUrlSigner.sign(returnUrl.toString());
				throw new RedirectToUrlException(signedUrl);
			}
		});
	}
}
