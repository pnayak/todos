/**
 * 
 */
package com.foobar.todos.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.api.security.User;
import com.github.pnayak.dropwizard.spring.DWProvider;
import com.yammer.dropwizard.auth.CachingAuthenticator;
import com.yammer.dropwizard.auth.oauth.OAuthProvider;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The FooBar OAuth provider
 * 
 */
@Component
@DWProvider
public class FoobarOAuthProvider extends OAuthProvider<User> {

	private static final String realm = "Foobar Super-Duper Security Realm";

	@Autowired
	public FoobarOAuthProvider(CachingAuthenticator<String, User> authenticator) {
		super(authenticator, realm);
	}
}
