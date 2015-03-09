package com.appdirect.isv.wicket.pages.authentication;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.core.context.SecurityContextHolder;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.wicket.util.RedirectToPageException;

@MountPath("logout")
public class LogoutPage extends WebPage {
	private static final long serialVersionUID = 3025667423327624281L;

	public LogoutPage(PageParameters parameters) {
		super(parameters);
		getSession().invalidateNow();
		SecurityContextHolder.clearContext();
		throw new RedirectToPageException(getApplication().getHomePage());
	}
}
