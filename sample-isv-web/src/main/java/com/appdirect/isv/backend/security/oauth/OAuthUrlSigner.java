package com.appdirect.isv.backend.security.oauth;

public interface OAuthUrlSigner {
	public String sign(String urlString);
}