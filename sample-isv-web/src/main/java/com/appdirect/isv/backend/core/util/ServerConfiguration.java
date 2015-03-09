package com.appdirect.isv.backend.core.util;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServerConfiguration {
	@Value("${appdirect.base.url}")
	private String appDirectBaseUrl;

	@Value("${oauth.consumer.key}")
	private String oAuthConsumerKey;

	@Value("${oauth.consumer.secret}")
	private String oAuthConsumerSecret;
}
