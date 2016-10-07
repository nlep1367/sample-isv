package com.appdirect.isv.config.security;

import javax.net.ssl.SSLContext;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeLayeredSocketFactory;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.Discovery;
import org.openid4java.discovery.html.HtmlResolver;
import org.openid4java.discovery.yadis.YadisResolver;
import org.openid4java.server.RealmVerifierFactory;
import org.openid4java.util.HttpCache;
import org.openid4java.util.HttpFetcher;
import org.openid4java.util.HttpFetcherFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.openid.AxFetchListFactory;
import org.springframework.security.openid.OpenID4JavaConsumer;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

import com.appdirect.isv.integration.util.ssl.SniSslSchemeLayeredSocketFactory;
import com.appdirect.isv.security.openid.OpenIDUserDetailsServiceImpl;
import com.google.common.collect.Lists;
import com.google.inject.Provider;

@Configuration
public class OpenIDConfiguration {
	@Bean
	public AuthenticationUserDetailsService<OpenIDAuthenticationToken> openIdUserDetailsService() {
		return new OpenIDUserDetailsServiceImpl();
	}

	@Bean
	public OpenID4JavaConsumer openIdConsumer() throws ConsumerException {
		OpenIDAttribute roleAttr = new OpenIDAttribute("roles", "https://www.appdirect.com/schema/user/roles");
		roleAttr.setCount(99);
		AxFetchListFactory attributesToFetchFactory = identifier -> Lists.newArrayList(
				new OpenIDAttribute("userUuid", "https://www.appdirect.com/schema/user/uuid"),
				new OpenIDAttribute("email", "http://axschema.org/contact/email"),
				new OpenIDAttribute("firstName", "http://axschema.org/namePerson/first"),
				new OpenIDAttribute("lastName", "http://axschema.org/namePerson/last"),
				new OpenIDAttribute("country", "http://axschema.org/contact/country/home"),
				new OpenIDAttribute("language", "http://axschema.org/pref/language"),
				roleAttr,
				new OpenIDAttribute("companyUuid", "https://www.appdirect.com/schema/company/uuid"),
				new OpenIDAttribute("companyName", "http://axschema.org/company/name"),
				new OpenIDAttribute("title", "http://axschema.org/company/title")
		);
		return new OpenID4JavaConsumer(consumerManager(), attributesToFetchFactory);
	}

	private ConsumerManager consumerManager() {
		SSLContext sslContext = null;
		X509HostnameVerifier hostnameVerifier = null;
		return new ConsumerManager(
			new RealmVerifierFactory(new YadisResolver(httpFetcherFactory(sslContext, hostnameVerifier))),
			new Discovery(
				new HtmlResolver(httpFetcherFactory(sslContext, hostnameVerifier)),
				new YadisResolver(httpFetcherFactory(sslContext, hostnameVerifier)),
				Discovery.getXriResolver()),
			httpFetcherFactory(sslContext, hostnameVerifier));
	}

	private HttpFetcherFactory httpFetcherFactory(SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {
		return new HttpFetcherFactory(httpCacheProvider(sslContext, hostnameVerifier));
	}

	private Provider<HttpFetcher> httpCacheProvider(SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {
		// in replacement of private org.openid4java.util.HttpFetcherFactory.HttpCacheProvider
		return () -> httpCache(sslContext, hostnameVerifier);
	}

	private HttpFetcher httpCache(SSLContext sslContext, X509HostnameVerifier hostnameVerifier) {
		HttpCache cache = new HttpCache(sslContext, hostnameVerifier);
		HttpClient client = (HttpClient) new DirectFieldAccessor(cache).getPropertyValue("_client");
		overrideSslSocketFactory(client);
		return cache;
	}

	@SuppressWarnings("deprecation")
	private void overrideSslSocketFactory(HttpClient client) {
		ClientConnectionManager connectionManager = client.getConnectionManager();
		SchemeRegistry schemeRegistry = connectionManager.getSchemeRegistry();
		Scheme scheme = schemeRegistry.getScheme("https");
		SchemeLayeredSocketFactory sslFactory = (SchemeLayeredSocketFactory) scheme.getSchemeSocketFactory();
		SniSslSchemeLayeredSocketFactory sniSslFactory = new SniSslSchemeLayeredSocketFactory(sslFactory);
		schemeRegistry.register(new Scheme("https", 443, sniSslFactory));
	}
}
