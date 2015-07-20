package com.appdirect.isv.security;

import java.util.Collection;
import java.util.Collections;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.appdirect.isv.dto.UserBean;

@Getter @RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = -1818866407181331464L;

	private final UserBean userBean;
	private final Collection<GrantedAuthority> authorities = Collections.unmodifiableList(AuthorityUtils.createAuthorityList("USER"));

	@Override
	public String getUsername() {
		return userBean.getEmail();
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}