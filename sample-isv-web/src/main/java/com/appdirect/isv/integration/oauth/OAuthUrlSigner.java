package com.appdirect.isv.integration.oauth;

public interface OAuthUrlSigner {
	public String sign(String urlString);
}