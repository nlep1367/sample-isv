package com.appdirect.isv.config;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServerConfiguration {
	@Value("${appdirect.base.url}")
	private String appDirectBaseUrl;

	@Value("${appdirect.oauth.consumer.key}")
	private String oAuthConsumerKey;

	@Value("${appdirect.oauth.consumer.secret}")
	private String oAuthConsumerSecret;
}
