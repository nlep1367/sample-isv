package com.appdirect.isv.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth.provider.ConsumerDetailsService;
import org.springframework.security.oauth.provider.filter.ProtectedResourceProcessingFilter;
import org.springframework.security.oauth.provider.token.InMemorySelfCleaningProviderTokenServices;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenServices;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.appdirect.isv.security.oauth.ConsumerDetailsServiceImpl;

@Configuration
public class OAuthConfiguration {
	@Bean
	public ConsumerDetailsService consumerDetailsService() {
		return new ConsumerDetailsServiceImpl();
	}

	@Bean
	public OAuthProviderTokenServices oauthProviderTokenServices() {
		return new InMemorySelfCleaningProviderTokenServices();
	}

	@Bean
	public ProtectedResourceProcessingFilter protectedResourceProcessingFilter() {
		ProtectedResourceProcessingFilter filter = new ProtectedResourceProcessingFilter();
		filter.setConsumerDetailsService(consumerDetailsService());
		filter.setTokenServices(oauthProviderTokenServices());
		return filter;
	}

	@Bean
	public AuthenticationEntryPoint authenticationEntryPoint() {
		return new Http403ForbiddenEntryPoint();
	}

	@Order(50)
	@Configuration
	public static class ApiConfiguration extends WebSecurityConfigurerAdapter {
		private static final String PATH = "/api/integration/appdirect/**";

		@Autowired
		private AuthenticationEntryPoint authenticationEntryPoint;
		@Autowired
		private ProtectedResourceProcessingFilter protectedResourceProcessingFilter;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.antMatcher(PATH)
					.sessionManagement()
							.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
							.and()
					.csrf().disable()
					.exceptionHandling()
							.authenticationEntryPoint(authenticationEntryPoint)
							.and()
					.authorizeRequests()
							.anyRequest().authenticated()
							.and()
					.addFilterBefore(protectedResourceProcessingFilter, UsernamePasswordAuthenticationFilter.class);
		}
	}

	@Order(60)
	@Configuration
	public static class WebConfiguration extends WebSecurityConfigurerAdapter {
		private static final String PATH = "/appdirect/**";

		@Autowired
		private AuthenticationEntryPoint authenticationEntryPoint;
		@Autowired
		private ProtectedResourceProcessingFilter protectedResourceProcessingFilter;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.antMatcher(PATH)
					.csrf().disable()
					.exceptionHandling()
							.authenticationEntryPoint(authenticationEntryPoint)
							.and()
					.authorizeRequests()
							.anyRequest().authenticated()
							.and()
					.addFilterBefore(protectedResourceProcessingFilter, UsernamePasswordAuthenticationFilter.class);
		}
	}
}
