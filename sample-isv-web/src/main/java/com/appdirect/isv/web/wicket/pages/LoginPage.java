package com.appdirect.isv.web.wicket.pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;

@MountPath("/login")
public class LoginPage extends BaseWebPage {
	private static final long serialVersionUID = -3022554881446343774L;

	public LoginPage(PageParameters parameters) {
		super(parameters);
		if (!parameters.get("error").isNull()) {
			String message = "N/A";
			HttpSession session = ((HttpServletRequest) getRequestCycle().getRequest().getContainerRequest()).getSession(false);
			if (session != null) {
				Object exception = session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
				if (exception != null) {
					message = exception.toString();
				}
			}
			error("An error occurred during login. Message: " + message);
		}
	}
}
