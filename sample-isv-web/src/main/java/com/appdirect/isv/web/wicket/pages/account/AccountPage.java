package com.appdirect.isv.web.wicket.pages.account;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.AddonBean;
import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.BaseWebPage;
import com.appdirect.isv.web.wicket.pages.authentication.SamlLoginPage;

@MountPath("/account/${accountId}")
public class AccountPage extends BaseWebPage {
	private static final long serialVersionUID = -1070718288315618917L;

	public static final String ACCOUNT_ID_PARAM = "accountId";

	@SpringBean
	private AccountService accountService;

	public AccountPage(final PageParameters parameters) {
		super(parameters);

		Long accountId = parameters.get(ACCOUNT_ID_PARAM).toLongObject();
		final AccountBean accountBean = accountService.readAccountByID(accountId);

		add(new Label("id", accountBean.getId()));
		add(new Label("uuid", accountBean.getUuid()));
		add(new Label("editionCode", accountBean.getEditionCode()));
		add(new Label("maxUsers", accountBean.getMaxUsers()));
		add(new Label("appDirectBaseUrl", accountBean.getAppDirectBaseUrl()));
		add(new Label("samlIdpEntityId", accountBean.getSamlIdpEntityId()));
		add(new Label("samlIdpMetadataUrl", accountBean.getSamlIdpMetadataUrl()));
		add(new AjaxLink<Void>("deleteAccountLink") {
			private static final long serialVersionUID = -278573087265133504L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				accountService.delete(accountBean);
				setResponsePage(getApplication().getHomePage());
			}
		});
		PageParameters samlLoginParameters = new PageParameters()
			.set(SamlLoginPage.ACCOUNT_ID_PARAM, accountBean.getId());
		add(new BookmarkablePageLink<Void>("samlLoginLink", SamlLoginPage.class, samlLoginParameters).setVisible(accountBean.getSamlIdpEntityId() != null));

		add(new DataView<AddonBean>("addonrow", new ListDataProvider<>(accountBean.getAddons())) {
			private static final long serialVersionUID = 4514620914249737635L;

			@Override
			public void populateItem(final Item<AddonBean> item) {
				final AddonBean addonBean = item.getModelObject();
				item.add(new Label("id", addonBean.getId()));
				item.add(new Label("identifier", addonBean.getAddonIdentifier()));
				item.add(new Label("code", addonBean.getCode()));
				item.add(new Label("quantity", addonBean.getQuantity()));
			}
		});

		add(new DataView<UserBean>("row", new ListDataProvider<>(accountBean.getUsers())) {
			private static final long serialVersionUID = 4514620914249737635L;

			@Override
			public void populateItem(final Item<UserBean> item) {
				final UserBean userBean = item.getModelObject();
				item.add(new Label("id", userBean.getId()));
				item.add(new Label("uuid", userBean.getUuid()));
				item.add(new Label("openId", userBean.getOpenId()));
				item.add(new Label("email", userBean.getEmail()));
				item.add(new Label("firstName", userBean.getFirstName()));
				item.add(new Label("lastName", userBean.getLastName()));
				item.add(new Label("zipCode", userBean.getZipCode()));
				item.add(new Label("department", userBean.getDepartment()));
				item.add(new Label("isAdmin", userBean.isAdmin()));
				item.add(new AjaxLink<Void>("deleteLink") {
					private static final long serialVersionUID = -278573087265133504L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						accountService.deleteUser(userBean.getId());
						setResponsePage(AccountPage.class, parameters);
					}
				});
			}
		});
	}
}
