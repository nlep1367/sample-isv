package com.appdirect.isv.integration.oauth;

import lombok.extern.slf4j.Slf4j;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.signature.QueryStringSigningStrategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuthUrlSignerImpl implements OAuthUrlSigner {
	private final OAuthConsumer consumer;

	@Autowired
	public OAuthUrlSignerImpl(@Value("${appdirect.oauth.consumer.key}") String oAuthConsumerKey, @Value("${appdirect.oauth.consumer.secret}") String oAuthConsumerSecret) {
		this.consumer = new DefaultOAuthConsumer(oAuthConsumerKey, oAuthConsumerSecret);
		this.consumer.setSigningStrategy(new QueryStringSigningStrategy());
	}

	@Override
	public String sign(String urlString) {
		log.debug("Signing URL: {}.", urlString);
		try {
			String signedUrl = consumer.sign(urlString);
			log.debug("Signed URL: {}.", signedUrl);
			return signedUrl;
		} catch (OAuthException e) {
			log.error("Error when signing URL", e);
			return urlString;
		}
	}
}
