package com.appdirect.isv.config;

import org.apache.wicket.protocol.http.WicketFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.appdirect.isv.web.wicket.application.WicketApplication;

@Configuration
public class WicketConfiguration {
	@Autowired
	private WicketApplication wicketApplication;

	@Bean
    public FilterRegistrationBean wicketFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new WicketFilter(wicketApplication));
        registration.setOrder(Integer.MAX_VALUE);
        registration.setName("wicket-filter");
        registration.addInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*");
        registration.addUrlPatterns("/*");
        return registration;
    }
}
