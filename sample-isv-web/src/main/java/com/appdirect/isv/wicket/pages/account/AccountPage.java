package com.appdirect.isv.wicket.pages.account;

import java.util.Arrays;
import java.util.UUID;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.backend.user.vo.AccountBean;
import com.appdirect.isv.backend.user.vo.AddonBean;
import com.appdirect.isv.backend.user.vo.UserBean;
import com.appdirect.isv.wicket.application.ISVApplication;
import com.appdirect.isv.wicket.pages.BaseWebPage;

@MountPath("accounts/${accountId}")
public class AccountPage extends BaseWebPage {
	private static final long serialVersionUID = -1070718288315618917L;

	public static final String ACCOUNT_ID_PARAM = "accountId";

	public AccountPage(final PageParameters parameters) {
		super(parameters);
		Long accountId = parameters.get(ACCOUNT_ID_PARAM).toLongObject();
		final AccountBean accountBean = ISVApplication.get().getIsvService().readAccountByID(accountId);
		add(new Label("id", new PropertyModel<Long>(accountBean, "id")));
		add(new Label("uuid", new PropertyModel<String>(accountBean, "uuid")));
		add(new Label("editionCode", new PropertyModel<String>(accountBean, "editionCode")));
		add(new Label("maxUsers", new PropertyModel<Integer>(accountBean, "maxUsers")));
		add(new Label("appDirectManaged", new PropertyModel<Boolean>(accountBean, "appDirectManaged")));
		add(new Label("appDirectBaseUrl", new PropertyModel<String>(accountBean, "appDirectBaseUrl")));
		add(new AjaxLink<Void>("deleteAccountLink") {
			private static final long serialVersionUID = -278573087265133504L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				ISVApplication.get().getIsvService().delete(accountBean);
				setResponsePage(ISVApplication.get().getHomePage());
			}
		});
		final DataView<AddonBean> addonDataView = new DataView<AddonBean>("addonrow", new ListDataProvider<AddonBean>(accountBean.getAddons())) {
			private static final long serialVersionUID = 4514620914249737635L;

			@Override
			public void populateItem(final Item<AddonBean> item) {
				item.add(new Label("id", new PropertyModel<Long>(item.getModelObject(), "id")));
				item.add(new Label("identifier", new PropertyModel<Long>(item.getModelObject(), "addonIdentifier")));
				item.add(new Label("code", new PropertyModel<String>(item.getModelObject(), "code")));
				item.add(new Label("quantity", new PropertyModel<String>(item.getModelObject(), "quantity")));
			}
		};
		add(addonDataView);
		final DataView<UserBean> usageDataView = new DataView<UserBean>("row", new ListDataProvider<UserBean>(accountBean.getUsers())) {
			private static final long serialVersionUID = 4514620914249737635L;

			@Override
			public void populateItem(final Item<UserBean> item) {
				item.add(new Label("id", new PropertyModel<Long>(item.getModelObject(), "id")));
				item.add(new Label("uuid", new PropertyModel<Long>(item.getModelObject(), "uuid")));
				item.add(new Label("openId", new PropertyModel<String>(item.getModelObject(), "openId")));
				item.add(new Label("email", new PropertyModel<String>(item.getModelObject(), "email")));
				item.add(new Label("firstName", new PropertyModel<String>(item.getModelObject(), "firstName")));
				item.add(new Label("lastName", new PropertyModel<String>(item.getModelObject(), "lastName")));
				item.add(new Label("zipCode", new PropertyModel<String>(item.getModelObject(), "zipCode")));
				item.add(new Label("department", new PropertyModel<String>(item.getModelObject(), "department")));
				item.add(new Label("isAdmin", new PropertyModel<Boolean>(item.getModelObject(), "isAdmin")));
				item.add(new AjaxLink<Void>("deleteLink") {
					private static final long serialVersionUID = -278573087265133504L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						ISVApplication.get().getIsvService().deleteUser(item.getModelObject().getId());
						setResponsePage(AccountPage.class, parameters);
					}
				});
			}
		};
		add(usageDataView);
		add(new AddUserForm("addUserForm", accountBean));
	}

	private static class AddUserForm extends Form<Void> {
		private static final long serialVersionUID = -8069294534860891266L;

		public AddUserForm(String id, final AccountBean accountBean) {
			super(id);
			final UserBean userBean = new UserBean();

			add(new TextField<String>("username", new PropertyModel<String>(userBean, "username")));
			add(new TextField<String>("password", new PropertyModel<String>(userBean, "password")));
			add(new TextField<String>("email", new PropertyModel<String>(userBean, "email")));
			add(new TextField<String>("firstName", new PropertyModel<String>(userBean, "firstName")));
			add(new TextField<String>("lastName", new PropertyModel<String>(userBean, "lastName")));
			add(new TextField<String>("zipCode", new PropertyModel<String>(userBean, "zipCode")));
			add(new TextField<String>("department", new PropertyModel<String>(userBean, "department")));
			add(new DropDownChoice<Boolean>("isAdmin", new PropertyModel<Boolean>(userBean, "isAdmin"), Arrays.asList(Boolean.TRUE, Boolean.FALSE)));

			add(new AjaxButton("addUserButton", this) {
				private static final long serialVersionUID = -3505373258148534293L;

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					userBean.setUuid(UUID.randomUUID().toString());
					ISVApplication.get().getIsvService().createUser(userBean, accountBean);
					PageParameters parameters = new PageParameters();
					parameters.set(AccountPage.ACCOUNT_ID_PARAM, accountBean.getId());
					setResponsePage(AccountPage.class, parameters);
				}
			});
		}
	}
}
