package com.pragyasystems.knowledgeHub.resources.user;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.foobar.todos.db.security.AccessTokenRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.security.AccessControlEntry;
import com.pragyasystems.knowledgeHub.api.security.AccessToken;
import com.pragyasystems.knowledgeHub.api.security.Group;
import com.pragyasystems.knowledgeHub.api.security.Principal;
import com.pragyasystems.knowledgeHub.api.security.Role;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.group.GroupRepository;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.space.LearningSpaceResource;
import com.pragyasystems.knowledgeHub.resources.space.LearningSpaceUtil;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.core.header.FormDataContentDisposition.FormDataContentDispositionBuilder;
import com.yammer.dropwizard.jersey.params.IntParam;

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
	private final SpaceRepository mockSpaceRepository = mock(SpaceRepository.class);
	private final GroupRepository mockGroupRepository = mock(GroupRepository.class);
	private final AccessTokenRepository mockTokenRepository = mock(AccessTokenRepository.class);
	@SuppressWarnings("unchecked")
	private final ElasticSearchIndex<User> mockUserIndex = mock(ElasticSearchIndex.class);
	@SuppressWarnings("unchecked")
	private final ElasticSearchIndex<LearningSpace> elasticSearchIndexForSpace = mock(ElasticSearchIndex.class);
	private final LearningSpaceUtil mockLearningSpaceUtil = mock(LearningSpaceUtil.class);
	
	private final UserResource resource = new UserResource(mockUserRepository,
			mockUserIndex, mockSpaceRepository, mockGroupRepository, 
			mockTokenRepository,
			mockLearningSpaceUtil);

	@SuppressWarnings("unused")
	private final LearningSpaceResource learningSpaceResource = new LearningSpaceResource(
			mockSpaceRepository, elasticSearchIndexForSpace,mockLearningSpaceUtil);

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
		user.setFirstName("Joe");
		user.setLastName("Smith");

		List<User> users = new ArrayList<User>();
		users.add(user);

		when(mockUserRepository.findByUsername("jsmith")).thenReturn(users);

	}

	@Test
	public void addNewUser() {
		final User user = new User();
		user.setUsername("jsmith");
		user.setPassword("foobar");
		user.setFirstName("Joe");
		user.setLastName("Smith");
		user.setMiddleName("W");
		user.setCity("Delhi");
		user.setCountry("India");
		user.setDescription("Demo User");
		user.seteMail("user@techizen.com");
		user.setLanguage("Hindi");
		user.setPicture("random binary data");
		user.setTimeZone("GMT+5:30");

		final Role role = new Role();
		role.setUuid("511846484800016990f86233");
		role.setRole("Student");
		role.setRoleDescription("A-FAKE-ROLE-DESCRIPTION");

		user.addRole(role);

		final User user2 = new User();
		user2.setUuid("511846484800016990f86233");
		user2.setVersion("A-FAKE-VERSION");
		user2.setUsername("jsmith");
		user2.setPassword("foobar");
		user2.setFirstName("Joe");
		user2.setLastName("Smith");
		user2.setMiddleName("W");
		user2.setCity("Delhi");
		user2.setCountry("India");
		user2.setDescription("Demo User");
		user2.seteMail("user@techizen.com");
		user2.setLanguage("Hindi");
		user2.setPicture("random binary data");
		user2.setTimeZone("GMT+5:30");

		user2.addRole(role);

		final LearningSpace learningSpace = new LearningSpace();
		learningSpace.setUuid("A-FAKE-LearningSpace-UUID");
		learningSpace.setVersion("A-FAKE-VERSION");
		learningSpace.setTitle("Test-Learning-Space");
		when(mockSpaceRepository.findOne(learningSpace.getUuid())).thenReturn(
				learningSpace);

		AccessControlEntry accessControlEntry = new AccessControlEntry();
		Principal principal = new Group();
		principal.setUuid("A-FAKE-principal-UUID");
		principal.setVersion("A-FAKE-principal-Version");
		accessControlEntry.setPrincipal(principal);

		learningSpace.addACE(accessControlEntry);

		when(mockUserRepository.save(user2)).thenReturn(user2);

		Response response = null; 
		/*response = resource.add("A-FAKE-LearningSpace-UUID", user2);

		assertEquals(response.getStatus(), 201);
		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");

		assert user2.getPassword().startsWith(UserUtils.PREFIX);*/

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			response = resource.add("A-FAKE-LearningSpace-UUID", null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// Bad incoming password
		try {
			user2.setPassword(UserUtils.hashPassword("foobar")); // set it to be
																	// already
																	// hashed
			response = resource.add("A-FAKE-LearningSpace-UUID", user2);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void updateUser() {

		final Role role = new Role();
		role.setUuid("511846484800016990f86233");
		role.setRole("A-FAKE-ROLE");
		role.setRoleDescription("A-FAKE-ROLE-DESCRIPTION");

		final User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("A-FAKE-VERSION");
		user.setUsername("jsmith");
		try {
			user.setPassword(UserUtils.hashPassword("foobar"));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		user.setFirstName("Joe");
		user.setLastName("Smith");
		user.addRole(role);

		final User user2 = new User();
		user2.setUuid("511846484800016990f86233");
		user2.setVersion("A-FAKE-VERSION");
		user2.setUsername("jsmith");
		user2.setPassword("foobar");
		user2.setFirstName("Joe");
		user2.setLastName("Smith");
		user2.addRole(role);

		final User user3 = new User();
		user3.setUuid(null);
		user2.setVersion("A-FAKE-VERSION");
		user2.setUsername("jsmith");
		user2.setPassword("foobar");
		user2.setFirstName("Joe");
		user2.setLastName("Smith");
		user2.addRole(role);

		when(mockUserRepository.findOne("511846484800016990f86233")).thenReturn(user);
		when(mockUserRepository.save(user2)).thenReturn(user2);

		Response response = resource.update("511846484800016990f86233", user2);

		assertEquals(response.getStatus(), 201);
		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		// Posted user cannot be null
		try {
			response = resource.update("511846484800016990f86233", null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// uuid in url cannot be null/empty
		try {
			response = resource.update(null, user);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// uuid of user in POST must not be null
		try {
			response = resource.update("511846484800016990f86233", user3);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// uuid of user in POST must match uuid in url
		try {
			response = resource.update("A-BAD-FAKE_UUID", user);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// username of provided user must match previously stored user
		try {
			user3.setUuid("A-FAKE_UUID");
			user3.setUsername("BAD-jsmith");
			response = resource.update("A-FAKE_UUID", user3);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// provided user must exist in the repository
		when(mockUserRepository.findOne("511846484800016990f86233")).thenReturn(null);
		try {
			response = resource.update("A-FAKE_UUID", user);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// Bad incoming password
		try {
			user2.setPassword(UserUtils.hashPassword("randomfoo")); // set it to
																	// be
																	// already
																	// hashed
			when(mockUserRepository.findOne("511846484800016990f86233")).thenReturn(user);
			when(mockUserRepository.save(user2)).thenReturn(user2);
			response = resource.update("511846484800016990f86233", user2);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void findSingleUserByGuid() {

		final User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("A-FAKE-VERSION");
		user.setUsername("jsmith");
		user.setPassword("foobar");
		user.setFirstName("Joe");
		user.setLastName("Smith");

		final Role role = new Role();
		role.setUuid("511846484800016990f86233");
		role.setRole("A-FAKE-ROLE");
		role.setRoleDescription("A-FAKE-ROLE-DESCRIPTION");

		user.addRole(role);

		when(mockUserRepository.findOne("511846484800016990f86233")).thenReturn(user);

		final User foundUser = resource.findOne("511846484800016990f86233");
		assertEquals(foundUser.getUuid(), "511846484800016990f86233");

		// Negative Testing
		try {
			resource.findOne(null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			when(mockUserRepository.findOne("511846484800016990f86233")).thenReturn(null);
			resource.findOne("511846484800016990f86233");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void findAllUsers() {

		final User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("A-FAKE-VERSION");
		user.setUsername("jsmith");
		user.setPassword("foobar");
		user.setFirstName("Joe");
		user.setLastName("Smith");

		final Role role = new Role();
		role.setUuid("511846484800016990f86233");
		role.setRole("A-FAKE-ROLE");
		role.setRoleDescription("A-FAKE-ROLE-DESCRIPTION");

		user.addRole(role);

		List<User> userList = Lists.newArrayList();
		userList.add(user);

		when(mockUserRepository.findAll()).thenReturn(userList);

		final Iterable<User> foundUserList = resource
				.findAll(new IntParam("10"));
		User foundUser = foundUserList.iterator().next();
		assertEquals(foundUser.getUuid(), "511846484800016990f86233");
		assertEquals(foundUser.getRoles().get(0).getUuid(), "511846484800016990f86233");

		// Negative Testing
		try {
			when(mockUserRepository.findAll()).thenReturn(null);
			resource.findAll(null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void deleteUser() {
		final User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("A-FAKE-VERSION");
		user.setUsername("jsmith");
		user.setPassword("foobar");
		user.setFirstName("Joe");
		user.setLastName("Smith");

		final Role role = new Role();
		role.setUuid("511846484800016990f86233");
		role.setRole("A-FAKE-ROLE");
		role.setRoleDescription("A-FAKE-ROLE-DESCRIPTION");

		user.addRole(role);

		final Response response = resource.delete("511846484800016990f86233");

		assertEquals(response.getStatus(), 204);

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = resource.delete(null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
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
		
		when(mockTokenRepository.save(any(AccessToken.class))).thenReturn(token);
		
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

/*	@Test
	public void test_findForParam() {

		final LearningSpace learningSpace = new LearningSpace();
		learningSpace.setUuid("A-FAKE-LearningSpace-UUID");
		learningSpace.setVersion("A-FAKE-VERSION");
		learningSpace.setTitle("Test-Learning-Space");

		when(mockSpaceRepository.findOne(learningSpace.getUuid())).thenReturn(
				learningSpace);

		// List<User> returnUserList = Lists.newArrayList();

		final User user2 = new User();
		user2.setUuid("50c21e18da06aaa3f8e596f7");
		user2.setVersion("A-FAKE-VERSION");
		user2.setUsername("jsmith");
		user2.setPassword("foobar");
		user2.setFirstName("Joe");
		user2.setLastName("Smith");

		Group group = new Group();
		group.setUuid("50c21e18da06aaa3f8e596f7");
		group.setName("SUPER_ADMIN");
		group.addUser(user2);

		List<Group> mockGroupList = Lists.newArrayList();
		mockGroupList.add(group);

		when(mockGroupRepository.findByName(any(String.class))).thenReturn(
				mockGroupList);

		when(mockUserRepository.findOne(any(String.class))).thenReturn(user2);

		resource.findForParam("A-FAKE-LearningSpace-UUID");

		// invalid password
		try {

			when(mockSpaceRepository.findOne(learningSpace.getUuid()))
					.thenReturn(null);

			resource.findForParam("A-FAKE-LearningSpace-UUID");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}*/

	@Test
	public void test_csvUserUpload() {
		String expected = "Underlying input stream returned zero bytes\n\nSuccessful Records : 0\nUnsuccessful Records : 1";
		InputStream mockInputStream = mock(InputStream.class);
		FormDataContentDispositionBuilder contentDispositionBuilder = FormDataContentDisposition
				.name("FAKE_CONTENT_DISPOSITION");
		FormDataContentDisposition mockContentDisposition = contentDispositionBuilder
				.fileName("FAKE_FILE.csv").build();

		String actual = resource.csvUserUpload("50c21e18da06aaa3f8e596f7",
				mockInputStream, mockContentDisposition);

		assertEquals(actual, expected);
	}

}
