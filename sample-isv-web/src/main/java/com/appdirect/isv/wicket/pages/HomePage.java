package com.appdirect.isv.wicket.pages;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.appdirect.isv.backend.user.vo.AccountBean;
import com.appdirect.isv.wicket.application.ISVApplication;
import com.appdirect.isv.wicket.pages.account.AccountPage;

public class HomePage extends BaseWebPage {
	private static final long serialVersionUID = -3213668296351926506L;

	public HomePage(PageParameters parameters) {
		super(parameters);
		add(new Label("oauthConsumerKey", ISVApplication.get().getServerConfiguration().getOAuthConsumerKey()));
		add(new Label("oauthConsumerSecret", ISVApplication.get().getServerConfiguration().getOAuthConsumerSecret()));
		List<AccountBean> accountBeans = ISVApplication.get().getIsvService().readAccounts();
		final DataView<AccountBean> dataView = new DataView<AccountBean>("row", new ListDataProvider<AccountBean>(accountBeans)) {
			private static final long serialVersionUID = 4514620914249737635L;

			@Override
			public void populateItem(final Item<AccountBean> item) {
				PageParameters parameters = new PageParameters();
				parameters.set(AccountPage.ACCOUNT_ID_PARAM, item.getModelObject().getId());
				BookmarkablePageLink<Void> idLink = new BookmarkablePageLink<Void>("idLink", AccountPage.class, parameters);
				idLink.add(new Label("id", new PropertyModel<Long>(item.getModelObject(), "id")));
				item.add(idLink);
				BookmarkablePageLink<Void> uuidLink = new BookmarkablePageLink<Void>("uuidLink", AccountPage.class, parameters);
				uuidLink.add(new Label("uuid", new PropertyModel<String>(item.getModelObject(), "uuid")));
				item.add(uuidLink);
				BookmarkablePageLink<Void> nameLink = new BookmarkablePageLink<Void>("nameLink", AccountPage.class, parameters);
				nameLink.add(new Label("name", new PropertyModel<String>(item.getModelObject(), "name")));
				item.add(nameLink);
				item.add(new Label("editionCode", new PropertyModel<String>(item.getModelObject(), "editionCode")));
				item.add(new Label("maxUsers", new PropertyModel<Integer>(item.getModelObject(), "maxUsers")));
				item.add(new Label("appDirectManaged", new PropertyModel<Boolean>(item.getModelObject(), "appDirectManaged")));
				item.add(new Label("appDirectBaseUrl", new PropertyModel<String>(item.getModelObject(), "appDirectBaseUrl")));
			}
		};
		add(dataView);
	}
}
