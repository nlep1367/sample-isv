package com.appdirect.isv.security.saml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appdirect.isv.integration.oauth.OAuthUrlSignerImpl;
import com.appdirect.isv.model.Account;
import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.repository.AccountRepository;

@Service
public class SamlMetadataLocationResolverImpl implements MetadataLocationResolver {
	@Autowired
	private AccountRepository accountRepository;

	/**
	 * This implementations turns AppDirect entity IDs into AppDirect metadata URLs and signs it for OAuth.
	 */
	@Override
	public String resolve(String entityId) {
		Account account = accountRepository.findBySamlIdpEntityId(entityId);
		if (account == null) {
			return null;
		}
		ApplicationProfile applicationProfile = account.getApplicationProfile();
		OAuthUrlSignerImpl oauthUrlSigner = new OAuthUrlSignerImpl(applicationProfile.getOauthConsumerKey(), applicationProfile.getOauthConsumerSecret());
		return oauthUrlSigner.sign(account.getSamlIdpMetadataUrl());
	}
}
