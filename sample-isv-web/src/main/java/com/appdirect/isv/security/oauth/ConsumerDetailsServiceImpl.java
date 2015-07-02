package com.appdirect.isv.security.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth.common.OAuthException;
import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.security.oauth.provider.ConsumerDetailsService;
import org.springframework.security.oauth.provider.InvalidOAuthParametersException;
import org.springframework.transaction.annotation.Transactional;

import com.appdirect.isv.model.ApplicationProfile;
import com.appdirect.isv.repository.ApplicationProfileRepository;

public class ConsumerDetailsServiceImpl implements ConsumerDetailsService {
	@Autowired
	private ApplicationProfileRepository applicationProfileRepository;

	@Override
	@Transactional(readOnly = true)
	public ConsumerDetails loadConsumerByConsumerKey(String consumerKey) throws OAuthException {
		ApplicationProfile applicationProfile = applicationProfileRepository.findByOauthConsumerKey(consumerKey);
		if (applicationProfile == null) {
			throw new InvalidOAuthParametersException("Consumer not found: " + consumerKey);
		}
		return new AppDirectConsumerDetails(applicationProfile);
	}
}
