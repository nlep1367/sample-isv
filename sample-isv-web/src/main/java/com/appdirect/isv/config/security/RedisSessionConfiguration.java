package com.appdirect.isv.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

@Configuration
public class RedisSessionConfiguration {
	@Bean
	public static ConfigureRedisAction configureRedisAction() {
		// Fixing a bug with the RedisHttpSessionConfiguration when Redis is not protected by a password
		return ConfigureRedisAction.NO_OP;
	}
}
