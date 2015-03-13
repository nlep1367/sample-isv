package com.appdirect.isv.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Configuration
public class DatabaseConfiguration {
	@Autowired
	private PlatformTransactionManager transactionManager;

	@PostConstruct
	public void configureTransactionManager() {
		((AbstractPlatformTransactionManager) transactionManager).setGlobalRollbackOnParticipationFailure(false);
	}
}
