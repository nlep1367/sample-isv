package com.appdirect.isv.wicket.pages.authentication;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.openid.OpenIDConsumerException;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.wicket.application.ISVApplication;
import com.appdirect.isv.wicket.pages.BaseWebPage;
import com.appdirect.isv.wicket.util.RedirectToPageException;

@Slf4j
@MountPath("login")
public class LoginPage extends BaseWebPage {
	private static final long serialVersionUID = -3022554881446343774L;

	public static final String OPENID_IDENTIFIER = "openid_identifier";

	public LoginPage(PageParameters parameters) {
		super(parameters);
		String openidIdentifier = parameters.get(OPENID_IDENTIFIER).toString();
		if (StringUtils.isNotBlank(openidIdentifier)) {
			startOpenIdLogin(openidIdentifier);
		}
		add(new FeedbackPanel("feedback"));
		Form<Void> openidForm = new Form<Void>("openidForm");
		final IModel<String> openidModel = new Model<String>();
		openidForm.add(new TextField<String>("openidField", openidModel));
		openidForm.add(new Button("openidButton") {
			private static final long serialVersionUID = 2088188745321523596L;

			@Override
			public void onSubmit() {
				String openid = StringUtils.trimToNull(openidModel.getObject());
				if (StringUtils.isNotBlank(openid)) {
					PageParameters parameters = new PageParameters();
					parameters.set(OPENID_IDENTIFIER, openid);
					throw new RedirectToPageException(LoginPage.class, parameters);
				}
			}
		});
		add(openidForm);
	}

	private void startOpenIdLogin(String openidIdentifier) {
		OpenIDConsumer consumer = ISVApplication.get().getOpenIdConsumer();
		HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
		String baseUrl = getBaseURL();
		String returnUrl = String.format("%s/%s", baseUrl, OpenIDReturnPage.MOUNT_PATH);
		try {
			String redirectUrl = consumer.beginConsumption(request, openidIdentifier, returnUrl, baseUrl);
			throw new RedirectToUrlException(redirectUrl);
		} catch (OpenIDConsumerException e) {
			log.error("OpenID exception: {}", e.getMessage(), e);
			error(String.format("Error occurred while trying to log in with %s.", openidIdentifier));
		}
	}

	private String getBaseURL() {
		String requestedUrl = ((HttpServletRequest) getRequest().getContainerRequest()).getRequestURL().toString();
		return requestedUrl.substring(0, requestedUrl.indexOf("/login"));
	}
}
