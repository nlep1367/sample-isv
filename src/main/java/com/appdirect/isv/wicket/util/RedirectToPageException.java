package com.appdirect.isv.wicket.util;

import org.apache.wicket.Page;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class RedirectToPageException extends RedirectToUrlException {
	private static final long serialVersionUID = 5033514168116148818L;

	public RedirectToPageException(Class<? extends Page> pageClass) {
		this(pageClass, null);
	}

	public RedirectToPageException(Class<? extends Page> pageClass, PageParameters parameters) {
		super(buildUrl(pageClass, parameters));
	}

	private static String buildUrl(Class<? extends Page> pageClass, PageParameters parameters) {
		return RequestCycle.get().urlFor(pageClass, parameters).toString();
	}
}
