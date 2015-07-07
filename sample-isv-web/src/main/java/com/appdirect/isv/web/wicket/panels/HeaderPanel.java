package com.appdirect.isv.web.wicket.panels;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.ObjectNotFoundException;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.AccountPage;
import com.appdirect.isv.web.wicket.pages.ApplicationProfilesPage;
import com.appdirect.isv.web.wicket.pages.LoginPage;
import com.appdirect.isv.web.wicket.pages.LogoutPage;
import com.appdirect.isv.web.wicket.pages.UsersPage;
import com.appdirect.isv.web.wicket.session.WicketSession;

public class HeaderPanel extends Panel {
	private static final long serialVersionUID = -9204859596313149030L;

	@SpringBean
	private AccountService accountService;

	public HeaderPanel(String id) {
		super(id);
		UserBean currentUser = WicketSession.get().getCurrentUser();
		AccountBean currentAccount;
		try {
			currentAccount = currentUser == null ? null : accountService.readAccountByUserID(currentUser.getId());
		} catch (ObjectNotFoundException onfe) {
			// Account or user was deleted.
			currentAccount = null;
		}
		add(new BookmarkablePageLink<Void>("home", getApplication().getHomePage()));
		PageParameters accountPageParameters = new PageParameters();
		if (currentAccount != null) {
			accountPageParameters.set(AccountPage.ACCOUNT_ID_PARAM, currentAccount.getId());
		}
		add(new BookmarkablePageLink<Void>("account", AccountPage.class, accountPageParameters).setVisible(currentAccount != null));
		add(new BookmarkablePageLink<Void>("users", UsersPage.class));
		add(new BookmarkablePageLink<Void>("applicationProfiles", ApplicationProfilesPage.class));
		add(new BookmarkablePageLink<Void>("login", LoginPage.class).setVisible(currentUser == null));
		add(new BookmarkablePageLink<Void>("logout", LogoutPage.class).setVisible(currentUser != null));

		WebMarkupContainer loginInfo = new WebMarkupContainer("loginInfo");
		if (currentUser != null) {
			loginInfo.add(new Label("openId", String.valueOf(currentUser.getOpenId())));
			loginInfo.add(new Label("firstName", currentUser.getFirstName()));
			loginInfo.add(new Label("lastName", currentUser.getLastName()));
		} else {
			loginInfo.setVisible(false);
		}
		add(loginInfo);
	}
}
