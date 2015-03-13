package com.appdirect.isv.web.wicket.session;

import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.appdirect.isv.dto.UserBean;
import com.appdirect.isv.security.UserDetailsImpl;

/**
 * Our custom web session implementation.
 */
public class WicketSession extends AbstractAuthenticatedWebSession {
	private static final long serialVersionUID = -3035882552790674791L;

	/**
	 * Returns the current session as an instance of this class.
	 *
	 * @return the current session
	 */
	public static WicketSession get() {
		return (WicketSession) AbstractAuthenticatedWebSession.get();
	}

	public WicketSession(Request request) {
		super(request);
	}

	@Override
	public Roles getRoles() {
		Roles roles = new Roles();
		if (isSignedIn()) {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			for (GrantedAuthority authority : authentication.getAuthorities()) {
				roles.add(authority.getAuthority());
			}
		}
		return roles;
	}

	public UserBean getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
			return null;
		}
		return ((UserDetailsImpl) authentication.getPrincipal()).getUserBean();
	}

	@Override
	public boolean isSignedIn() {
		return getCurrentUser() != null;
	}
}
