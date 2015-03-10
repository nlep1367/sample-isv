package com.appdirect.isv.web.wicket.pages;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.ObjectNotFoundException;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.account.AccountPage;
import com.appdirect.isv.web.wicket.pages.account.UsersPage;
import com.appdirect.isv.web.wicket.pages.authentication.LoginPage;
import com.appdirect.isv.web.wicket.pages.authentication.LogoutPage;
import com.appdirect.isv.web.wicket.session.WicketSession;

public abstract class BaseWebPage extends WebPage {
	private static final long serialVersionUID = -3364237183755496911L;

	@SpringBean
	private AccountService accountService;

	public BaseWebPage() {
		this(new PageParameters());
	}

	public BaseWebPage(PageParameters parameters) {
		super(parameters);

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

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));
		UserBean currentUser = WicketSession.get().getCurrentUser();
		AccountBean currentAccount;
		try {
			currentAccount = currentUser == null ? null : accountService.readAccountByUserID(currentUser.getId());
		} catch (ObjectNotFoundException onfe) {
			// Account or user was deleted.
			currentAccount = null;
		}
		if (currentAccount != null && StringUtils.isNotBlank(currentAccount.getAppDirectBaseUrl())) {
			response.render(JavaScriptHeaderItem.forUrl(String.format("%s/widgets/myapps.js", currentAccount.getAppDirectBaseUrl())));
		}
	}
}
