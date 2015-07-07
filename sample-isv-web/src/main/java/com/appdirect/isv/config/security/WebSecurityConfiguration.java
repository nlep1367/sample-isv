package com.appdirect.isv.config.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLEntryPoint;
import org.springframework.security.saml.SAMLProcessingFilter;
import org.springframework.security.saml.metadata.MetadataDisplayFilter;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	private AuthenticationUserDetailsService<OpenIDAuthenticationToken> openIdUserDetailsService;
	@Autowired
	private OpenIDConsumer openIdConsumer;
	@Autowired
	private SAMLUserDetailsService samlUserDetailsService;
	@Autowired
	private SavedRequestAwareAuthenticationSuccessHandler samlSuccessRedirectHandler;
	@Autowired
	private SimpleUrlAuthenticationFailureHandler samlAuthenticationFailureHandler;
	@Autowired
	private SAMLEntryPoint samlEntryPoint;
	@Autowired
	private MetadataDisplayFilter samlMetadataDisplayFilter;
	@Autowired
	private MetadataGeneratorFilter samlMetadataGeneratorFilter;

	@Bean
	public SecurityContextHolderStrategy securityContextHolderStrategy() {
		return SecurityContextHolder.getContextHolderStrategy();
	}

	@Bean
	public SAMLAuthenticationProvider samlAuthenticationProvider() {
		SAMLAuthenticationProvider samlAuthenticationProvider = new SAMLAuthenticationProvider();
		samlAuthenticationProvider.setUserDetails(samlUserDetailsService);
		samlAuthenticationProvider.setForcePrincipalAsString(false);
		return samlAuthenticationProvider;
	}

	@Bean
	public SAMLProcessingFilter samlWebSsoProcessingFilter() throws Exception {
		SAMLProcessingFilter samlWebSSOProcessingFilter = new SAMLProcessingFilter();
		samlWebSSOProcessingFilter.setAuthenticationManager(authenticationManagerBean());
		samlWebSSOProcessingFilter.setAuthenticationSuccessHandler(samlSuccessRedirectHandler);
		samlWebSSOProcessingFilter.setAuthenticationFailureHandler(samlAuthenticationFailureHandler);
		return samlWebSSOProcessingFilter;
	}

	@Bean
	public FilterChainProxy samlFilterChain() throws Exception {
		List<SecurityFilterChain> chains = new ArrayList<>();
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SAMLEntryPoint.FILTER_URL + "/**"), samlEntryPoint));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(MetadataDisplayFilter.FILTER_URL + "/**"), samlMetadataDisplayFilter));
		chains.add(new DefaultSecurityFilterChain(new AntPathRequestMatcher(SAMLProcessingFilter.FILTER_URL + "/**"), samlWebSsoProcessingFilter()));
		return new FilterChainProxy(chains);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
						.anyRequest().permitAll()
						.and()
				.csrf().disable()
				.logout()
						.logoutSuccessUrl("/")
						.and()
				.openidLogin()
						.loginPage("/login").permitAll()
						.loginProcessingUrl("/openid")
						.authenticationUserDetailsService(openIdUserDetailsService)
						.consumer(openIdConsumer)
						.and()
				.addFilterBefore(samlMetadataGeneratorFilter, ChannelProcessingFilter.class)
				.addFilterAfter(samlFilterChain(), BasicAuthenticationFilter.class);
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(samlAuthenticationProvider());
	}

	@Override
	@Bean(name = "authenticationManager")
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManager();
	}
}
