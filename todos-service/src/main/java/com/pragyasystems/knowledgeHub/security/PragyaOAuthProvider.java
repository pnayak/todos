/**
 * 
 */
package com.pragyasystems.knowledgeHub.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.pnayak.dropwizard.spring.DWProvider;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.yammer.dropwizard.auth.CachingAuthenticator;
import com.yammer.dropwizard.auth.oauth.OAuthProvider;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The Pragya OAuth provider
 * 
 */
@Component
@DWProvider
public class PragyaOAuthProvider extends OAuthProvider<User> {

	private static final String realm = "Pragya KnowledgeHub Security";

	@Autowired
	public PragyaOAuthProvider(CachingAuthenticator<String, User> authenticator) {
		super(authenticator, realm);
	}
}
