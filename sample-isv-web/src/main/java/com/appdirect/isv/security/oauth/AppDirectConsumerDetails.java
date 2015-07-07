package com.appdirect.isv.security.oauth;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth.common.signature.SharedConsumerSecretImpl;
import org.springframework.security.oauth.common.signature.SignatureSecret;
import org.springframework.security.oauth.provider.ExtraTrustConsumerDetails;

import com.appdirect.isv.model.ApplicationProfile;

@Getter @AllArgsConstructor
public class AppDirectConsumerDetails implements ExtraTrustConsumerDetails {
	private static final long serialVersionUID = -6956240437430543968L;

	@NonNull
	private final ApplicationProfile applicationProfile;

	@Override
	public String getConsumerKey() {
		return applicationProfile.getOauthConsumerKey();
	}

	@Override
	public String getConsumerName() {
		return applicationProfile.getUrl();
	}

	@Override
	public SignatureSecret getSignatureSecret() {
		return new SharedConsumerSecretImpl(applicationProfile.getOauthConsumerSecret());
	}

	@Override
	public List<GrantedAuthority> getAuthorities() {
		return Collections.emptyList();
	}

	@Override
	public boolean isRequiredToObtainAuthenticatedToken() {
		return false;
	}
}
