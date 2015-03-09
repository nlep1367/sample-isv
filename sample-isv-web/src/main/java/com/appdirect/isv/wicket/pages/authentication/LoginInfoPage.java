package com.appdirect.isv.wicket.pages.authentication;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.backend.user.vo.UserBean;
import com.appdirect.isv.wicket.pages.BaseWebPage;
import com.appdirect.isv.wicket.session.ISVSession;

@MountPath("info")
public class LoginInfoPage extends BaseWebPage {
	private static final long serialVersionUID = -4651649027900582969L;

	public LoginInfoPage(PageParameters parameters) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof OpenIDAuthenticationToken) {
			OpenIDAuthenticationToken openIdToken = (OpenIDAuthenticationToken) authentication;
			PageableListView<OpenIDAttribute> openIdAttrList = new PageableListView<OpenIDAttribute>("row", openIdToken.getAttributes(), 20) {
				private static final long serialVersionUID = 8007445373834575271L;

				@Override
				protected void populateItem(ListItem<OpenIDAttribute> item) {
					item.add(new Label("key", new Model<String>(item.getModelObject().getName())));
					item.add(new Label("value", new Model<String>(item.getModelObject().getValues().get(0))));
				}
			};
			add(openIdAttrList);
		} else {
			UserBean currentUser = ISVSession.get().getCurrentUser();
			WebMarkupContainer row = new WebMarkupContainer("row");
			row.add(new Label("key", new Model<String>("username")));
			row.add(new Label("value", new Model<String>(currentUser == null ? "N/A" : currentUser.getUuid())));
			add(row);
		}
	}
}
