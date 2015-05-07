package com.appdirect.isv.web.wicket.pages;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import com.appdirect.isv.config.ServerConfiguration;
import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.account.AccountPage;

public class HomePage extends BaseWebPage {
	private static final long serialVersionUID = -3213668296351926506L;

	@SpringBean
	private ServerConfiguration serverConfiguration;
	@SpringBean
	private AccountService accountService;

	public HomePage(PageParameters parameters) {
		super(parameters);

		add(new Label("oauthConsumerKey", serverConfiguration.getOAuthConsumerKey()));
		add(new Label("oauthConsumerSecret", serverConfiguration.getOAuthConsumerSecret()));

		List<AccountBean> accountBeans = accountService.readAccounts();

		add(new DataView<AccountBean>("row", new ListDataProvider<>(accountBeans)) {
			private static final long serialVersionUID = 4514620914249737635L;

			@Override
			public void populateItem(final Item<AccountBean> item) {
				AccountBean accountBean = item.getModelObject();
				PageParameters parameters = new PageParameters()
						.set(AccountPage.ACCOUNT_ID_PARAM, accountBean.getId());
				item.add(new BookmarkablePageLink<Void>("idLink", AccountPage.class, parameters)
						.add(new Label("id", accountBean.getId())));
				item.add(new BookmarkablePageLink<Void>("uuidLink", AccountPage.class, parameters)
						.add(new Label("uuid", accountBean.getUuid())));
				item.add(new Label("editionCode", accountBean.getEditionCode()));
				item.add(new Label("maxUsers", accountBean.getMaxUsers()));
				item.add(new Label("appDirectBaseUrl", accountBean.getAppDirectBaseUrl()));
			}
		});
	}
}
