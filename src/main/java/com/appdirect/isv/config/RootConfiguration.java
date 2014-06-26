package com.appdirect.isv.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.appdirect.isv.backend.BackendPackage;
import com.appdirect.isv.wicket.application.ISVApplication;

@Configuration
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy
@ComponentScan(basePackageClasses = { RootConfiguration.class, BackendPackage.class, ISVApplication.class })
@ImportResource({
	"classpath:spring-beans/applicationContext-orm.xml",
	"classpath:spring-beans/applicationContext-security.xml",
	"classpath:spring-beans/applicationContext-cxf.xml"
})
@PropertySource("classpath:server.properties")
public class RootConfiguration {
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer bean = new PropertySourcesPlaceholderConfigurer();
		return bean;
	}
}
