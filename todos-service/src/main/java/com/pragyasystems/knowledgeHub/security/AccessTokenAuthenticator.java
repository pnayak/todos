package com.pragyasystems.knowledgeHub.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.foobar.todos.db.security.AccessTokenRepository;
import com.google.common.base.Optional;
import com.pragyasystems.knowledgeHub.api.security.AccessToken;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.yammer.dropwizard.auth.AuthenticationException;
import com.yammer.dropwizard.auth.Authenticator;

public class AccessTokenAuthenticator implements Authenticator<String, User> {

	private static final Logger LOG = LoggerFactory.getLogger(AccessTokenAuthenticator.class);
	@Autowired
	private AccessTokenRepository tokenRepository;

	@Override
	public Optional<User> authenticate(String token)
			throws AuthenticationException {
		
		LOG.debug("************* AccessTokenAuthenticator invoked ************");
		
		AccessToken storedToken = tokenRepository.findOne(token);
		
		if (storedToken != null && storedToken.isValid()) {
			User authenticatedUser = storedToken.getUser();
			return Optional.of(authenticatedUser);
		} 
		
		return Optional.absent();
	}

	/**
	 * @param tokenRepository
	 *            the tokenRepository to set
	 */
	public void setTokenRepository(AccessTokenRepository tokenRepository) {
		this.tokenRepository = tokenRepository;
	}
}
