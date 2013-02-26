package com.pragyasystems.knowledgeHub.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.foobar.todos.constants.Constants;
import com.google.common.collect.Lists;
import com.pragyasystems.knowledgeHub.api.security.AccessControlEntry;
import com.pragyasystems.knowledgeHub.api.security.Group;
import com.pragyasystems.knowledgeHub.api.security.Role;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.group.GroupRepository;

public class AuthUtilTest {

	private GroupRepository mockGroupRepository = mock(GroupRepository.class);

	private Role superAdminRole;
	private Role instAdminRole;
	private Role instructorRole;
	private Role studentRole;
	private final AuthUtil authZ = new AuthUtil(mockGroupRepository);

	@BeforeClass
	public void init() {

		// SuperAdmin role
		superAdminRole = new Role();
		superAdminRole.setUuid("superAdminRole-42-2012");
		superAdminRole.setRole(Constants.ROLE_SUPER_ADMIN);
		authZ.SUPERADMIN = superAdminRole;

		// Institution Admin role
		instAdminRole = new Role();
		instAdminRole.setUuid("adminRole-42-2012");
		instAdminRole.setRole(Constants.ROLE_ADMIN);
		authZ.ADMIN = instAdminRole;

		// Instructor role
		instructorRole = new Role();
		instructorRole.setUuid("instructorRole-42-2012");
		instructorRole.setRole(Constants.ROLE_INSTRUCTOR);
		authZ.INSTRUCTOR = instructorRole;

		// Student role
		studentRole = new Role();
		studentRole.setUuid("studentRole-42-2012");
		studentRole.setRole(Constants.ROLE_STUDENT);
		authZ.STUDENT = studentRole;
	}

	@Test
	public void hasRole() {

		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);

		assertTrue(authZ.hasRole(user, authZ.INSTRUCTOR),
				"User is  in instructor role");
		assertFalse(authZ.hasRole(user, authZ.SUPERADMIN),
				"User is not in superAdmin role");
		assertFalse(authZ.hasRole(user, authZ.ADMIN),
				"User is not in admin role");
		assertFalse(authZ.hasRole(user, authZ.STUDENT),
				"User is not in student role");
	}

	@Test
	public void assertRole() {

		User user = new User("fake-user-uuid");
		user.addRole(authZ.ADMIN);
		
		authZ.assertHasRole(user, authZ.ADMIN);

		try {
			authZ.assertHasRole(user, authZ.STUDENT);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void hasAnyOfRoles() {

		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);
		user.addRole(authZ.ADMIN);

		assertTrue(authZ.hasAnyOfRoles(user, authZ.INSTRUCTOR, authZ.ADMIN),
				"User is in instructor or admin role");
		assertTrue(authZ.hasAnyOfRoles(user, authZ.STUDENT, authZ.INSTRUCTOR),
				"User is not in superAdmin role");
		assertTrue(authZ.hasAnyOfRoles(user, authZ.ADMIN, authZ.SUPERADMIN),
				"User is not in superAdmin role");
		assertTrue(authZ.hasAnyOfRoles(user, authZ.ADMIN),
				"User is not in admin role");
		assertFalse(authZ.hasAnyOfRoles(user, authZ.STUDENT, authZ.SUPERADMIN),
				"User is not in student or superadmin role");
	}

	@Test
	public void assertAnyOfRoles() {

		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);
		user.addRole(authZ.ADMIN);

		authZ.assertHasAnyOfRoles(user, authZ.ADMIN, authZ.STUDENT);
		
		try {
			authZ.assertHasAnyOfRoles(user, authZ.STUDENT);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void canReadForUser() {

		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);

		AccessControlEntry readACE = new AccessControlEntry();
		readACE.setRead(Boolean.TRUE);
		readACE.setPrincipal(user);

		List<AccessControlEntry> aceList = Lists.newArrayList();
		aceList.add(readACE);

		LearningSpace space = new LearningSpace();
		space.addACE(readACE);

		assertTrue(authZ.canRead(user, space),
				"User has read permission on space");
		assertFalse(authZ.canWrite(user, space),
				"User does not have write permission on space");
		
		User anotherUser = new User("fake-anotherUser-uuid");
		anotherUser.addRole(authZ.INSTRUCTOR);
		
		assertFalse(authZ.canRead(anotherUser, space),
				"User does not have read permission on space");
	}
	
	@Test
	public void assertCanReadForUser() {
		
		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);

		AccessControlEntry readACE = new AccessControlEntry();
		readACE.setRead(Boolean.TRUE);
		readACE.setPrincipal(user);

		List<AccessControlEntry> aceList = Lists.newArrayList();
		aceList.add(readACE);

		LearningSpace space = new LearningSpace();
		space.addACE(readACE);
		
		authZ.assertCanRead(user, space);
		
		User anotherUser = new User("fake-anotherUser-uuid");
		anotherUser.addRole(authZ.INSTRUCTOR);
		
		try {
			authZ.assertCanRead(anotherUser, space);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void canReadForGroup() {

		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);

		Group instructorGroup = new Group();
		instructorGroup.setUuid("fake-group-uuid");
		instructorGroup.setName("InstructorGroup");
		instructorGroup.addUser(user);
		user.addGroup(instructorGroup);

		when(mockGroupRepository.findOne("fake-group-uuid")).thenReturn(
				instructorGroup);

		AccessControlEntry readACE = new AccessControlEntry();
		readACE.setRead(Boolean.TRUE);
		readACE.setPrincipal(instructorGroup);

		List<AccessControlEntry> aceList = Lists.newArrayList();
		aceList.add(readACE);

		LearningSpace space = new LearningSpace();
		space.addACE(readACE);

		assertTrue(authZ.canRead(user, space),
				"User has read permission on space");
		assertFalse(authZ.canWrite(user, space),
				"User does not have write permission on space");
		
		User anotherUser = new User("fake-anotherUser-uuid");
		anotherUser.addRole(authZ.INSTRUCTOR);
		
		Group anotherGroup = new Group();
		anotherGroup.setUuid("fake-anotherGroup-uuid");
		anotherGroup.setName("AnotherInstructorGroup");
		anotherGroup.addUser(anotherUser);
		anotherUser.addGroup(anotherGroup);

		when(mockGroupRepository.findOne("fake-anotherGroup-uuid")).thenReturn(
				anotherGroup);
		
		assertFalse(authZ.canRead(anotherUser, space),
				"User does not have read permission on space");

	}

	@Test
	public void canWriteForUser() {

		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);

		AccessControlEntry writeACE = new AccessControlEntry();
		writeACE.setWrite(Boolean.TRUE);
		writeACE.setPrincipal(user);

		List<AccessControlEntry> aceList = Lists.newArrayList();
		aceList.add(writeACE);

		LearningSpace space = new LearningSpace();
		space.addACE(writeACE);

		assertTrue(authZ.canWrite(user, space),
				"User has write permission on space");
		assertFalse(authZ.canRead(user, space),
				"User does not have read permission on space");
		
		User anotherUser = new User("fake-anotherUser-uuid");
		anotherUser.addRole(authZ.INSTRUCTOR);
		
		assertFalse(authZ.canWrite(anotherUser, space),
				"User does not have read permission on space");
	}
	
	@Test
	public void assertCanWriteForUser() {
		
		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);

		AccessControlEntry writeACE = new AccessControlEntry();
		writeACE.setWrite(Boolean.TRUE);
		writeACE.setPrincipal(user);

		List<AccessControlEntry> aceList = Lists.newArrayList();
		aceList.add(writeACE);

		LearningSpace space = new LearningSpace();
		space.addACE(writeACE);
		
		authZ.assertCanWrite(user, space);
		
		User anotherUser = new User("fake-anotherUser-uuid");
		anotherUser.addRole(authZ.INSTRUCTOR);
		
		try {
			authZ.assertCanWrite(anotherUser, space);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void canWriteForGroup() {

		User user = new User("fake-user-uuid");
		user.addRole(authZ.INSTRUCTOR);

		Group instructorGroup = new Group();
		instructorGroup.setUuid("fake-group-uuid");
		instructorGroup.setName("InstructorGroup");
		instructorGroup.addUser(user);
		user.addGroup(instructorGroup);

		when(mockGroupRepository.findOne("fake-group-uuid")).thenReturn(
				instructorGroup);

		AccessControlEntry writeACE = new AccessControlEntry();
		writeACE.setWrite(Boolean.TRUE);
		writeACE.setPrincipal(instructorGroup);

		List<AccessControlEntry> aceList = Lists.newArrayList();
		aceList.add(writeACE);

		LearningSpace space = new LearningSpace();
		space.addACE(writeACE);

		assertTrue(authZ.canWrite(user, space),
				"User has write permission on space");
		assertFalse(authZ.canRead(user, space),
				"User does not have read permission on space");
		
		User anotherUser = new User("fake-anotherUser-uuid");
		anotherUser.addRole(authZ.INSTRUCTOR);
		
		Group anotherGroup = new Group();
		anotherGroup.setUuid("fake-anotherGroup-uuid");
		anotherGroup.setName("AnotherInstructorGroup");
		anotherGroup.addUser(anotherUser);
		anotherUser.addGroup(anotherGroup);

		when(mockGroupRepository.findOne("fake-anotherGroup-uuid")).thenReturn(
				anotherGroup);
		
		assertFalse(authZ.canWrite(anotherUser, space),
				"User does not have read permission on space");
	}
	
	@Test
	public void testSuperAdminReadandWrite(){
		User user = new User("fake-user-uuid");
		user.addRole(authZ.SUPERADMIN);
		LearningSpace space = new LearningSpace();
		assertTrue(authZ.canWrite(user, space),
				"Super Admin User must have write permission regardless of ACE");
		
		assertTrue(authZ.canRead(user, space),
				"Super Admin User must have read permission regardless of ACE");
		
	}
	
}
