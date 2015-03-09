package com.appdirect.isv.wicket.pages.account;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.backend.user.vo.UserBean;
import com.appdirect.isv.wicket.application.ISVApplication;
import com.appdirect.isv.wicket.pages.BaseWebPage;

@MountPath("userlist")
public class UserListPage extends BaseWebPage {
	private static final long serialVersionUID = 1778281200402519646L;

	public UserListPage(PageParameters parameters) {
		super(parameters);
		List<UserBean> userBeans = ISVApplication.get().getIsvService().readUsers();
		final DataView<UserBean> dataView = new DataView<UserBean>("row", new ListDataProvider<UserBean>(userBeans)) {
			private static final long serialVersionUID = -1432601596719602292L;

			@Override
			public void populateItem(final Item<UserBean> item) {
				final UserBean userBean = item.getModelObject();
				item.add(new Label("id", String.valueOf(userBean.getId())));
				item.add(new Label("uuid", userBean.getUuid()));
				item.add(new Label("openId", userBean.getOpenId()));
				item.add(new Label("email", userBean.getEmail()));
				item.add(new Label("firstName", userBean.getFirstName()));
				item.add(new Label("lastName", userBean.getLastName()));
				item.add(new Label("zipCode", userBean.getZipCode()));
				item.add(new Label("department", userBean.getDepartment()));
				item.add(new Label("isAdmin", String.valueOf(userBean.isAdmin())));
			}
		};
		add(dataView);
	}
}
