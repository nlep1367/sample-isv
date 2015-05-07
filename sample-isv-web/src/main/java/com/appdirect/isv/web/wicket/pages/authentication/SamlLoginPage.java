package com.appdirect.isv.web.wicket.pages.authentication;

import java.nio.charset.StandardCharsets;

import org.apache.wicket.request.flow.RedirectToUrlException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.saml.SAMLEntryPoint;
import org.wicketstuff.annotation.mount.MountPath;

import com.appdirect.isv.dto.AccountBean;
import com.appdirect.isv.service.AccountService;
import com.appdirect.isv.web.wicket.pages.BaseWebPage;

/**
 * This landing page used to login using an specific account SAML IDP.
 * When a user lands in this page the proper SAML login URL is constructed using
 * the account's associated IDP entity ID and the user user is redirected to it
 * initiating the SP initiated login flow.
 *
 * @author enrique.cambio
 *
 */
@MountPath("/account/${accountId}/saml/login")
public class SamlLoginPage extends BaseWebPage {
	private static final long serialVersionUID = -5803243150952646876L;

	public static final String ACCOUNT_ID_PARAM = "accountId";

	@SpringBean
	private AccountService accountService;

	public SamlLoginPage(final PageParameters parameters) {
		super(parameters);
		long accountId = parameters.get(ACCOUNT_ID_PARAM).toLong();
		AccountBean accountBean = accountService.readAccountByID(accountId);
		if (accountBean == null || accountBean.getSamlIdpEntityId() == null) {
			throw new AccessDeniedException("Account " + accountId + " is not SAML-enabled.");
		}
		String encodedIdpEntityId = UrlEncoder.QUERY_INSTANCE.encode(accountBean.getSamlIdpEntityId(), StandardCharsets.UTF_8);
		throw new RedirectToUrlException(SAMLEntryPoint.FILTER_URL + "?" + SAMLEntryPoint.IDP_PARAMETER + "=" + encodedIdpEntityId);
	}
}
