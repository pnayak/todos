/**
 * 
 */
package com.foobar.todos.resources.security;

import static com.foobar.todos.resources.security.UserUtils.passwordValid;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.api.security.AccessToken;
import com.foobar.todos.api.security.User;
import com.foobar.todos.db.security.AccessTokenRepository;
import com.foobar.todos.db.security.UserRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.foobar.todos.resources.BaseResource;
import com.yammer.metrics.annotation.Timed;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The resource that provides access to learning content
 * 
 */
@Component
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends BaseResource<User> {

	private static final Logger LOG = LoggerFactory
			.getLogger(UserResource.class);
	private UserRepository userRepository;
	private ElasticSearchIndex<User> userIndex;
	private AccessTokenRepository tokenRepository;

	/**
	 * @param contentRepository
	 */
	@Autowired
	public UserResource(UserRepository userRepository,
			ElasticSearchIndex<User> elasticSearchIndexForUser,
			AccessTokenRepository tokenRespository) {
		super(userRepository, elasticSearchIndexForUser);
		this.userRepository = userRepository;
		this.userIndex = elasticSearchIndexForUser;
		this.tokenRepository = tokenRespository;
	}

	@POST
	@Timed
	@Path("/login")
	public User login(User providedUser) {

		if (providedUser == null || providedUser.getUsername() == null
				|| providedUser.getPassword() == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		LOG.debug("finding User with userName=> [" + providedUser.getUsername()
				+ "]");
		// This should contain only one item, username is unique.
		List<User> users = userRepository.findByUsername(providedUser
				.getUsername());
		try {
			if (users != null
					&& !users.isEmpty()
					&& passwordValid(providedUser.getPassword(), users.get(0)
							.getPassword())) {

				User authenticatedUser = users.get(0);

				// Create a new access token and save it
				AccessToken token = new AccessToken();
				token.setUser(authenticatedUser);
				AccessToken validToken = tokenRepository.save(token);

				// For now, return the token in the users password field
				authenticatedUser.setPassword(validToken.getUuid());

				return authenticatedUser;
			} else {
				throw new WebApplicationException(Status.UNAUTHORIZED);
			}
		} catch (Exception e) {
			throw new WebApplicationException(e, Status.UNAUTHORIZED);
		}
	}
}
