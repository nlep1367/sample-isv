package com.appdirect.isv.backend.security.oauth;

import lombok.extern.slf4j.Slf4j;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthException;
import oauth.signpost.signature.QueryStringSigningStrategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.appdirect.isv.backend.core.util.ServerConfiguration;

@Slf4j
@Component("oauthUrlSigner")
public class OAuthUrlSignerImpl implements OAuthUrlSigner {
	private final OAuthConsumer consumer;

	@Autowired
	public OAuthUrlSignerImpl(ServerConfiguration serverConfiguration) {
		consumer = new DefaultOAuthConsumer(serverConfiguration.getOAuthConsumerKey(), serverConfiguration.getOAuthConsumerSecret());
		consumer.setSigningStrategy(new QueryStringSigningStrategy());
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
