package com.appdirect.isv.backend.security.oauth;

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
import org.springframework.stereotype.Component;

import com.appdirect.isv.backend.core.util.ServerConfiguration;

@Slf4j
@Component("oauthPhaseInterceptor")
public class OAuthPhaseInterceptor<T extends Message> extends AbstractPhaseInterceptor<T> {
	@Autowired
	private ServerConfiguration serverConfiguration;

	private OAuthConsumer consumer;

	public OAuthPhaseInterceptor() {
		super(Phase.SEND);
	}

	@Override
	public void handleMessage(T message) throws Fault {
		if (consumer == null) {
			consumer = new DefaultOAuthConsumer(serverConfiguration.getOAuthConsumerKey(), serverConfiguration.getOAuthConsumerSecret());
		}
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
