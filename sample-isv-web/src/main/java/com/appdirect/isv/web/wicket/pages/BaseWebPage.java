package com.appdirect.isv.web.wicket.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.appdirect.isv.web.wicket.panels.HeaderPanel;

public abstract class BaseWebPage extends WebPage {
	private static final long serialVersionUID = -3364237183755496911L;

	private static final String FEEDBACK_ID = "feedback";

	public BaseWebPage() {
		super();
	}

	public BaseWebPage(PageParameters parameters) {
		super(parameters);
	}

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new HeaderPanel("header"));
		add(new FeedbackPanel(FEEDBACK_ID).setOutputMarkupId(true));
	}

	protected FeedbackPanel getFeedbackPanel() {
		return (FeedbackPanel) get(FEEDBACK_ID);
	}
}
