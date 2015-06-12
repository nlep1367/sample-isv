package com.appdirect.isv.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.appdirect.isv.integration.oauth.OAuthUrlSigner;
import com.appdirect.isv.model.Account;
import com.appdirect.isv.repository.AccountRepository;
import com.appdirect.isv.security.saml.MetadataLocationResolver;

@Service
public class SamlMetadataLocationResolverImpl implements MetadataLocationResolver {
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private OAuthUrlSigner oauthUrlSigner;

	/**
	 * This implementations turns AppDirect entity IDs into AppDirect metadata URLs and signs it for OAuth.
	 */
	@Override
	public String resolve(String entityId) {
		Account account = accountRepository.findBySamlIdpEntityId(entityId);
		if (account == null) {
			return null;
		}
		return oauthUrlSigner.sign(account.getSamlIdpMetadataUrl());
	}
}
