package com.appdirect.isv.integration.oauth;

import java.net.HttpURLConnection;
import java.net.URL;

import lombok.extern.slf4j.Slf4j;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthException;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.HTTPConduit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OAuthPhaseInterceptor extends AbstractPhaseInterceptor<Message> {
	private final OAuthConsumer consumer;

	@Autowired
	public OAuthPhaseInterceptor(@Value("${appdirect.oauth.consumer.key}") String consumerKey, @Value("${appdirect.oauth.consumer.secret}") String consumerSecret) {
		super(Phase.SEND);
		this.consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
	}

	@Override
	public void handleMessage(Message message) throws Fault {
		log.debug("Entering handleMessage");
		HttpURLConnection connect = (HttpURLConnection) message.get(HTTPConduit.KEY_HTTP_CONNECTION);
		if (connect == null) {
			return;
		}
		URL url = connect.getURL();
		if (url == null) {
			return;
		}
		log.debug("Request: {}", url);
		try {
			consumer.sign(connect);
			log.debug("Request: {} signed", url);
		} catch (OAuthException e) {
			throw new Fault(e);
		}
	}
}
