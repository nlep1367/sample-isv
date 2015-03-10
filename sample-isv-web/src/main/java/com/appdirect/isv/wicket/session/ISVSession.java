package com.appdirect.isv.wicket.session;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.Request;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.hibernate.ObjectNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.appdirect.isv.backend.security.service.UserDetailsImpl;
import com.appdirect.isv.backend.user.vo.UserBean;

/**
 * Our custom web session implementation.
 */
public class ISVSession extends AuthenticatedWebSession {
	private static final long serialVersionUID = -3035882552790674791L;

	@SpringBean(name = "authenticationManager")
	private AuthenticationManager authenticationManager;

	public ISVSession(Request request) {
		super(request);
		Injector.get().inject(this);
	}

	@Override
	public boolean authenticate(String username, String password) {
		throw new NotImplementedException("Username and password login is not available.");
	}

	public boolean authenticate(Authentication token) {
		boolean authenticated = false;
		try {
			Authentication authentication = authenticationManager.authenticate(token);
			SecurityContextHolder.getContext().setAuthentication(authentication);
			authenticated = authentication.isAuthenticated();
		} catch (AuthenticationException e) {
			error(String.format("User \"%s\" failed to login. Reason: %s.", token.getName(), e.getMessage()));
		} catch (ObjectNotFoundException onfe) {
			error(String.format("User \"%s\" could not be found.", token.getName()));
		}
		signIn(authenticated);
		return authenticated;
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

	/**
	 * Returns the current session as an instance of this class.
	 *
	 * @return the current session
	 */
	public static ISVSession get() {
		return (ISVSession) AuthenticatedWebSession.get();
	}

	public UserBean getCurrentUser() {
		if (isSignedIn()) {
			return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserBean();
		} else {
			return null;
		}
	}
}
