package com.appdirect.isv.config;

import org.openid4java.consumer.ConsumerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.openid.AxFetchListFactory;
import org.springframework.security.openid.OpenID4JavaConsumer;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

import com.appdirect.isv.security.OpenIDUserDetailsServiceImpl;
import com.google.common.collect.Lists;

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
		return new OpenID4JavaConsumer(attributesToFetchFactory);
	}
}
