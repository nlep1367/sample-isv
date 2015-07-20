package com.appdirect.isv.web.wicket.pages;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.service.AccountService;

@MountPath("/users")
public class UsersPage extends BaseWebPage {
	private static final long serialVersionUID = 1778281200402519646L;

	@SpringBean
	private AccountService accountService;

	public UsersPage(PageParameters parameters) {
		super(parameters);
		List<UserBean> userBeans = accountService.readUsers();
		add(new DataView<UserBean>("row", new ListDataProvider<>(userBeans)) {
			private static final long serialVersionUID = -1432601596719602292L;

			@Override
			public void populateItem(final Item<UserBean> item) {
				final UserBean userBean = item.getModelObject();
				item.add(new Label("id", String.valueOf(userBean.getId())));
				item.add(new Label("appDirectOpenId", userBean.getAppDirectOpenId()));
				item.add(new Label("email", userBean.getEmail()));
				item.add(new Label("firstName", userBean.getFirstName()));
				item.add(new Label("lastName", userBean.getLastName()));
				item.add(new Label("zipCode", userBean.getZipCode()));
				item.add(new Label("department", userBean.getDepartment()));
				item.add(new Label("isAdmin", userBean.isAdmin()));
			}
		});
	}
}
