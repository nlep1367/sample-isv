package com.appdirect.isv.config.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Timer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.velocity.app.VelocityEngine;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.util.resource.ResourceException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.SAMLBootstrap;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLRelayStateSuccessHandler;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLContextProviderImpl;
import org.springframework.security.saml.key.EmptyKeyManager;
import org.springframework.security.saml.key.KeyManager;
import org.springframework.security.saml.log.SAMLDefaultLogger;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGenerator;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.security.saml.parser.ParserPoolHolder;
import org.springframework.security.saml.processor.HTTPPostBinding;
import org.springframework.security.saml.processor.SAMLBinding;
import org.springframework.security.saml.processor.SAMLProcessorImpl;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.saml.util.VelocityFactory;
import org.springframework.security.saml.websso.WebSSOProfile;
import org.springframework.security.saml.websso.WebSSOProfileConsumer;
import org.springframework.security.saml.websso.WebSSOProfileConsumerHoKImpl;
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl;
import org.springframework.security.saml.websso.WebSSOProfileImpl;
import org.springframework.security.saml.websso.WebSSOProfileOptions;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.appdirect.isv.security.saml.HttpMetadataProviderLoader;
import com.appdirect.isv.security.saml.MetadataLocationResolver;
import com.appdirect.isv.security.saml.MetadataProviderLoader;
import com.appdirect.isv.security.saml.OnDemandMetadataManager;
import com.appdirect.isv.security.saml.SamlMetadataLocationResolverImpl;
import com.appdirect.isv.security.saml.SamlUserDetailsServiceImpl;

@Configuration
public class SAMLConfiguration {
	private static final String SAML_SP_ENTITY_ID = "https://sample-isv.appdirect.com";

	@Bean
	public AuthenticationSuccessHandler samlAuthenticationSuccessHandler() {
		SAMLRelayStateSuccessHandler successRedirectHandler = new SAMLRelayStateSuccessHandler();
		successRedirectHandler.setDefaultTargetUrl("/");
		return successRedirectHandler;
	}

	@Bean
	public SimpleUrlAuthenticationFailureHandler samlAuthenticationFailureHandler() {
		SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();
		failureHandler.setUseForward(true);
		failureHandler.setDefaultFailureUrl("/error");
		return failureHandler;
	}

	@Bean
	public MetadataDisplayFilter samlMetadataDisplayFilter() {
		return new MetadataDisplayFilter();
	}

	@Bean
	public MetadataGeneratorFilter samlMetadataGeneratorFilter() {
		return new MetadataGeneratorFilter(samlMetadataGenerator());
	}

	@Bean
	public SAMLEntryPoint samlEntryPoint() {
		SAMLEntryPoint samlEntryPoint = new SAMLEntryPoint();
		samlEntryPoint.setDefaultProfileOptions(samlWebSsoProfileOptions());
		return samlEntryPoint;
	}

	@Bean
	public KeyManager samlKeyManager() {
		return new EmptyKeyManager();
	}

	@Bean
	public ExtendedMetadata samlSpExtendedMetadata() {
		ExtendedMetadata extendedMetadata = new ExtendedMetadata();
		extendedMetadata.setIdpDiscoveryEnabled(false);
		extendedMetadata.setSignMetadata(false);
		extendedMetadata.setSigningKey(null);
		extendedMetadata.setEncryptionKey(null);
		return extendedMetadata;
	}

	@Bean
	public SAMLUserDetailsService samlUserDetailsService() {
		return new SamlUserDetailsServiceImpl();
	}

	@Bean
	public VelocityEngine samlVelocityEngine() {
		return VelocityFactory.getEngine();
	}

	@Bean(initMethod = "initialize")
	public StaticBasicParserPool samlParserPool() {
		return new StaticBasicParserPool();
	}

	@Bean(name = "parserPoolHolder")
	public ParserPoolHolder samlParserPoolHolder() {
		return new ParserPoolHolder();
	}

	@Bean
	public static SAMLBootstrap samlBootstrap() {
		return new SAMLBootstrap();
	}

	@Bean
	public SAMLContextProvider samlContextProvider() {
		return new SAMLContextProviderImpl();
	}

	@Bean
	public SAMLDefaultLogger samlLogger() {
		return new SAMLDefaultLogger();
	}

	@Bean(name = "webSSOprofileConsumer")
	public WebSSOProfileConsumer samlWebSsoProfileConsumer() {
		return new WebSSOProfileConsumerImpl();
	}

	@Bean(name = "hokWebSSOprofileConsumer")
	public WebSSOProfileConsumerHoKImpl samlWebSsoProfileConsumerHok() {
		return new WebSSOProfileConsumerHoKImpl();
	}

	@Bean(name = "webSSOprofile")
	public WebSSOProfile samlWebSsoProfile() {
		return new WebSSOProfileImpl();
	}

	@Bean
	public WebSSOProfileOptions samlWebSsoProfileOptions() {
		WebSSOProfileOptions webSSOProfileOptions = new WebSSOProfileOptions();
		webSSOProfileOptions.setIncludeScoping(false);
		return webSSOProfileOptions;
	}

	@Bean(destroyMethod = "cancel")
	public Timer samlMetadataProviderTimer() {
		return new Timer("samlMetadataProviderTimer", true);
	}

	@Bean(name = "metadata")
	public MetadataManager samlIdpsMetadata() throws MetadataProviderException, ResourceException {
		long maxCacheSize = 1000;
		long cacheExpirationMins = 10;
		return new OnDemandMetadataManager(
				samlMetadataResolver(),
				samlIdpMetadataProviderLoader(),
				maxCacheSize,
				cacheExpirationMins,
				Collections.emptyList());
	}

	@Bean
	public MetadataProviderLoader samlIdpMetadataProviderLoader() {
		return new HttpMetadataProviderLoader(
				samlMetadataProviderTimer(),
				samlIdpMetadataHttpClient(),
				samlParserPool());
	}

	@Bean
	public MultiThreadedHttpConnectionManager samlIdpMetadataConnectionManager() {
		return new MultiThreadedHttpConnectionManager();
	}

	@Bean
	public HttpClient samlIdpMetadataHttpClient() {
		return new HttpClient(samlIdpMetadataConnectionManager());
	}

	@Bean
	public MetadataLocationResolver samlMetadataResolver() {
		return new SamlMetadataLocationResolverImpl();
	}

	@Bean
	public MetadataGenerator samlMetadataGenerator() {
		MetadataGenerator metadataGenerator = new MetadataGenerator();
		metadataGenerator.setEntityId(SAML_SP_ENTITY_ID);
		metadataGenerator.setEntityBaseURL("https://dev-sample-isv.devappdirect.me/");
		metadataGenerator.setExtendedMetadata(samlSpExtendedMetadata());
		metadataGenerator.setIncludeDiscoveryExtension(false);
		metadataGenerator.setRequestSigned(false);
		metadataGenerator.setKeyManager(samlKeyManager());
		return metadataGenerator;
	}

	@Bean
	public HTTPPostBinding samlHttpPostBinding() {
		return new HTTPPostBinding(samlParserPool(), samlVelocityEngine());
	}

	@Bean
	public SAMLProcessorImpl samlProcessor() {
		Collection<SAMLBinding> bindings = new ArrayList<>();
		bindings.add(samlHttpPostBinding());
		return new SAMLProcessorImpl(bindings);
	}
}
