package com.appdirect.isv.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.metadata.MetadataGeneratorFilter;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.stereotype.Component;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	/*
	 * This class is used to break the dependency cycle between SecurityConfiguration and SamlConfiguration
	 */
	@Component
	public static class AuthenticationManagerDelegate {
		private AuthenticationManager delegate;
		public AuthenticationManager get() {
			return authentication -> delegate.authenticate(authentication);
		};
	}

	@Autowired
	private AuthenticationUserDetailsService<OpenIDAuthenticationToken> openIdUserDetailsService;
	@Autowired
	private OpenIDConsumer openIdConsumer;
	@Autowired
	private MetadataGeneratorFilter samlMetadataGeneratorFilter;
	@Autowired
	@Qualifier("samlFilterChain")
	private FilterChainProxy samlFilterChain;
	@Autowired
	private SAMLAuthenticationProvider samlAuthenticationProvider;
	@Autowired
	private AuthenticationManagerDelegate authenticationManagerDelegate;

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
				.addFilterAfter(samlFilterChain, BasicAuthenticationFilter.class);

		authenticationManagerDelegate.delegate = authenticationManager();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(samlAuthenticationProvider);
	}

	@Bean
	@Override
	public AuthenticationManager authenticationManager() throws Exception {
		return super.authenticationManager();
	}
}
