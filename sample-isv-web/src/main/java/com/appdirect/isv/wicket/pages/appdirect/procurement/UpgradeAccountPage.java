package com.appdirect.isv.wicket.pages.appdirect.procurement;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.hibernate.ObjectNotFoundException;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.backend.integration.remote.type.ErrorCode;
import com.appdirect.isv.backend.integration.remote.type.EventType;
import com.appdirect.isv.backend.integration.remote.vo.EventInfo;
import com.appdirect.isv.backend.user.vo.AccountBean;
import com.appdirect.isv.wicket.application.ISVApplication;

@MountPath("appdirect/upgrade")
public class UpgradeAccountPage extends BaseIntegrationPage {
	private static final long serialVersionUID = 1193297070006588092L;

	public UpgradeAccountPage(PageParameters parameters) {
		super(parameters);

		final EventInfo eventInfo = readEvent(parameters);

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
				AccountBean accountBean = new AccountBean();
				accountBean.setUuid(eventInfo.getPayload().getAccount().getAccountIdentifier());
				accountBean.setEditionCode(eventInfo.getPayload().getOrder().getEditionCode());
				accountBean.setMaxUsers(eventInfo.getPayload().getOrder().getMaxUsers());
				StringBuffer returnUrl = new StringBuffer(eventInfo.getReturnUrl());;
				try {
					ISVApplication.get().getIsvService().update(accountBean);
					returnUrl.append("&success=true");
				} catch (ObjectNotFoundException onfe) {
					returnUrl.append("&success=false&errorCode=").append(ErrorCode.ACCOUNT_NOT_FOUND.toString());
				}
				String signedUrl = ISVApplication.get().getOauthUrlSigner().sign(returnUrl.toString());
				throw new RedirectToUrlException(signedUrl);
			}
		});
	}
}
