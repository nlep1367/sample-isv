package com.appdirect.isv.config;

import java.util.Collections;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionTrackingMode;

import lombok.extern.slf4j.Slf4j;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.spring.SpringWebApplicationFactory;
import org.springframework.web.context.AbstractContextLoaderInitializer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.filter.RequestContextFilter;

@Slf4j
public class ContextLoaderInitializerImpl extends AbstractContextLoaderInitializer {
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		super.onStartup(servletContext);

		servletContext.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.COOKIE));

		// This filter needs to be the first one to run to properly set the charset encoding on the request / response.
		log.info("Registering the charset filter.");
		FilterRegistration.Dynamic charsetRegistration = servletContext.addFilter("charsetFilter", CharacterEncodingFilter.class);
		charsetRegistration.setInitParameter("encoding", "UTF-8");
		charsetRegistration.setInitParameter("forceEncoding", "true");
		charsetRegistration.addMappingForUrlPatterns(null, false, "/*");

		log.info("Registering the request context filter.");
		// Needed so that we can access contextual request information from Spring beans.
		servletContext.addFilter("requestContext", RequestContextFilter.class)
				.addMappingForUrlPatterns(null, false, "/*");

		log.info("Registering the Spring Security filter chain.");
		servletContext.addFilter("springSecurityFilterChain", DelegatingFilterProxy.class)
				.addMappingForUrlPatterns(null, false, "/*");

		log.info("Registering the CXF servlet.");
		ServletRegistration.Dynamic cxfRegistration = servletContext.addServlet("cxf", new CXFServlet());
		cxfRegistration.setInitParameter("service-list-path", "web-services");
		cxfRegistration.setLoadOnStartup(1);
		cxfRegistration.addMapping("/api/*");

		log.info("Registering the Wicket filter.");
		FilterRegistration.Dynamic wicketRegistration = servletContext.addFilter("wicket", WicketFilter.class);
		wicketRegistration.setInitParameter(WicketFilter.APP_FACT_PARAM, SpringWebApplicationFactory.class.getCanonicalName());
		wicketRegistration.setInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/*");
		wicketRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ERROR), true, "/*");
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		log.info("Creating the root application context.");
		AnnotationConfigWebApplicationContext rootApplicationContext = new AnnotationConfigWebApplicationContext();
		rootApplicationContext.register(RootConfiguration.class);
		return rootApplicationContext;
	}
}
