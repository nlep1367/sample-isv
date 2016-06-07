package com.appdirect.isv.web.api;

import java.io.IOException;
import java.net.HttpURLConnection;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.client.SimpleClientHttpRequestFactory;

import com.appdirect.isv.model.ApplicationProfile;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthException;

@Slf4j
public class TwoLeggedOAuthClientHttpRequestFactory extends SimpleClientHttpRequestFactory {
	private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10_000;
	private static final int DEFAULT_READ_TIMEOUT_MILLIS = 30_000;

	private final OAuthConsumer consumer;

	public TwoLeggedOAuthClientHttpRequestFactory(ApplicationProfile applicationProfile) {
		consumer = new DefaultOAuthConsumer(applicationProfile.getOauthConsumerKey(), applicationProfile.getOauthConsumerSecret());
		setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS);
		setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS);

		// Disable output streaming to avoid "org.springframework.web.client.ResourceAccessException: I/O error on POST request for [URL]: cannot retry due to server authentication, in streaming mode; nested exception is java.net.HttpRetryException: cannot retry due to server authentication, in streaming mode"
		setOutputStreaming(false);
	}

	@Override
	protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
		super.prepareConnection(connection, httpMethod);
		try {
			consumer.sign(connection);
		} catch (OAuthException e) {
			log.error("Error while signing request", e);
		}
	}
}
