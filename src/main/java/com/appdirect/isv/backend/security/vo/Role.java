package com.appdirect.isv.backend.security.vo;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
	USER, ADMIN;

	@Override
	public String getAuthority() {
		return toString();
	}
}
