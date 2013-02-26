package com.foobar.todos.resources.security;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.foobar.todos.api.security.AccessToken;
import com.foobar.todos.api.security.User;
import com.foobar.todos.db.security.AccessTokenRepository;
import com.foobar.todos.db.security.UserRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.foobar.todos.resources.security.UserResource;
import com.foobar.todos.resources.security.UserUtils;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Test the UserResource, specifically its methods to add, update, find,
 *         delete, etc.
 */
@Test(groups = { "checkintest" })
public class UserResourceTest {

	private final UserRepository mockUserRepository = mock(UserRepository.class);
	private final AccessTokenRepository mockTokenRepository = mock(AccessTokenRepository.class);
	@SuppressWarnings("unchecked")
	private final ElasticSearchIndex<User> mockUserIndex = mock(ElasticSearchIndex.class);
	@SuppressWarnings("unchecked")
	private final UserResource resource = new UserResource(mockUserRepository,
			mockUserIndex, mockTokenRepository);


	@BeforeMethod
	public void before() {
		final User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("A-FAKE-VERSION");
		user.setUsername("jsmith");
		try {
			user.setPassword(UserUtils.hashPassword("foobar"));
		} catch (Exception e) {
			e.printStackTrace();
		}


		List<User> users = new ArrayList<User>();
		users.add(user);

		when(mockUserRepository.findByUsername("jsmith")).thenReturn(users);

	}

	/**
	 * Look common mock call n before method
	 */
	@Test
	public void login() {

		final User providedUser = new User();
		providedUser.setUsername("jsmith");
		providedUser.setPassword("foobar");

		final AccessToken token = new AccessToken("511846484800016990f86233");

		when(mockTokenRepository.save(any(AccessToken.class)))
				.thenReturn(token);

		final User loggedInUser = resource.login(providedUser);
		assertEquals(loggedInUser.getUuid(), "511846484800016990f86233");
		assertEquals(loggedInUser.getPassword(), "511846484800016990f86233");
	}

	/**
	 * Look common mock call n before method
	 */
	@Test
	public void login_negativeTesting() {

		// Negative Testing
		try {
			resource.login(null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
		// with null username and password
		final User providedUser = new User();
		try {
			resource.login(providedUser);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
		// null password
		providedUser.setUsername("jsmith");
		try {
			resource.login(providedUser);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// invalid username
		providedUser.setUsername("invalid");
		providedUser.setPassword("foobar");
		try {
			resource.login(providedUser);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// invalid password
		providedUser.setUsername("jsmith");
		providedUser.setPassword("password");
		try {
			resource.login(providedUser);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}
}
