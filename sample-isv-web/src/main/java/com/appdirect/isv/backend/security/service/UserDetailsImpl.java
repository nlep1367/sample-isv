package com.appdirect.isv.backend.security.service;

import java.util.Collection;
import java.util.Collections;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.appdirect.isv.backend.security.vo.Role;
import com.appdirect.isv.backend.user.vo.UserBean;

@Getter @RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = -1818866407181331464L;

	private final UserBean userBean;

	@Override
	public String getUsername() {
		return userBean.getUuid();
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return Collections.singleton((GrantedAuthority) Role.USER);
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