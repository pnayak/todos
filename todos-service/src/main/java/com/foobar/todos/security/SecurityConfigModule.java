/**
 * 
 */
package com.foobar.todos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.foobar.todos.ToDoServiceConfiguration;
import com.foobar.todos.api.security.User;
import com.foobar.todos.db.security.AccessTokenRepository;
import com.google.common.cache.CacheBuilderSpec;
import com.yammer.dropwizard.auth.CachingAuthenticator;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         This class represents the required beans to connect to MongoDB
 */
@Configuration
//@EnableAspectJAutoProxy
public class SecurityConfigModule {

	@Autowired
	protected AccessTokenRepository tokenRepository;
	@Autowired
	protected ToDoServiceConfiguration configuration;

	@Bean
	protected CachingAuthenticator<String, User> getCachingAuthenticator() {

		AccessTokenAuthenticator authenticator = new AccessTokenAuthenticator();
		authenticator.setTokenRepository(this.tokenRepository);

		CacheBuilderSpec cacheBuilderSpec = CacheBuilderSpec
				.parse(configuration.getAuthenticationCachePolicy());

		return CachingAuthenticator.wrap(authenticator, cacheBuilderSpec);
	}
	
//    @Bean 
//    public AuthAspect authAspect() {
//        return new AuthAspect();
//    }
}
