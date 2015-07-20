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

@MountPath("/appdirect/upgrade")
public class UpgradeAccountPage extends BaseIntegrationPage {
	private static final long serialVersionUID = 1193297070006588092L;

	@SpringBean
	private AccountService accountService;
	@SpringBean
	private OAuthUrlSigner oauthUrlSigner;

	public UpgradeAccountPage(PageParameters parameters) {
		super(parameters);

		EventInfo eventInfo = readEvent(parameters);
		if (eventInfo == null || eventInfo.getType() != EventType.SUBSCRIPTION_CHANGE) {
			throw new IllegalStateException("Invalid event object.");
		}

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
				AccountBean accountBean = new AccountBean(applicationProfile);
				accountBean.setAppDirectUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
				accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
				accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
				StringBuffer returnUrl = new StringBuffer(eventInfo.getReturnUrl());;
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
	}
}
