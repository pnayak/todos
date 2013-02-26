/**
 * 
 */
package com.pragyasystems.knowledgeHub.security;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.foobar.todos.constants.Constants;
import com.pragyasystems.knowledgeHub.api.Collection;
import com.pragyasystems.knowledgeHub.api.security.AccessControlEntry;
import com.pragyasystems.knowledgeHub.api.security.Group;
import com.pragyasystems.knowledgeHub.api.security.Principal;
import com.pragyasystems.knowledgeHub.api.security.Role;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.db.group.GroupRepository;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Provide utility functions to check authZ
 */
public class AuthUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(AuthUtil.class);
	
	public static Role SUPERADMIN;
	public static Role ADMIN;
	public static Role INSTRUCTOR;
	public static Role STUDENT;
	
	private GroupRepository groupRepository;

	/**
	 * @param groupRepository
	 */
	@Autowired
	public AuthUtil(GroupRepository groupRepository) {
		super();
		this.groupRepository = groupRepository;
		LOG.debug("AuthUtil initialized with groupRepository " + this.groupRepository);
	}

	/**
	 * Use to check if a User has a specific Role
	 * 
	 * @param user
	 * @param role
	 * @return
	 */
	public boolean hasRole(User user, Role role) {

		List<Role> userRolesList = user.getRoles();

		for (Role userRole : userRolesList) {
			if (userRole.equals(role)) {
				LOG.debug("User has expected role" + userRole + " = " + role);
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * Helper for hasRole() that throws an unauthorized exception
	 * 
	 * @param user
	 * @param role
	 */
	public void assertHasRole(User user, Role role) {
		if (!hasRole(user, role)) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}
	
	/**
	 * Use to check if a User has either one of a list of roles
	 * 
	 * @param user
	 * @param role
	 * @return
	 */
	public boolean hasAnyOfRoles(User user, Role... roles) {

		List<Role> userRolesList = user.getRoles();

		for (Role userRole : userRolesList) {
			for (Role role : roles) {
				if (userRole.equals(role)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Helper for hasAnyOfRoles() that throws an unauthorized exception
	 * 
	 * @param user
	 * @param role
	 */
	public void assertHasAnyOfRoles(User user, Role... role) {
		if (!hasAnyOfRoles(user, role)) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}

	/**
	 * Use to check if a User can read a Collection (likely a LearningSpace,
	 * Topic, etc.)
	 * 
	 * @param user
	 * @param entity
	 * @param repository
	 * @return
	 */
	public boolean canRead(User user, Collection entity) {
		if(isSuperAdmin(user)){
			return Boolean.TRUE;
		}

		List<AccessControlEntry> aceList = entity.getACEList();

		for (AccessControlEntry ace : aceList) {
			// If the ace allows READ and if the user is the Principal of ACE
			// then return true
			if (ace.canRead() == Boolean.TRUE
					&& userIsPrincipalOfACE(user, ace)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
	
	/**
	 * 
	 * Helper for canRead() that throws an unauthorized exception
	 * 
	 * @param user
	 * @param entity
	 */
	public void assertCanRead(User user, Collection entity) {
		if (!canRead(user, entity)) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}

	/**
	 * Use to check if a User can write to a Collection (likely a LearningSpace,
	 * Topic, etc.)
	 * 
	 * @param user
	 * @param entity
	 * @param repository
	 * @return
	 */
	public boolean canWrite(User user, Collection entity) {
		
		if(isSuperAdmin(user)){
			return Boolean.TRUE;
		}
		
		List<AccessControlEntry> aceList = entity.getACEList();
		for (AccessControlEntry ace : aceList) {
			// If the ace allows READ and if the user is the Principal of ACE
			// then return true
			if (ace.canWrite() == Boolean.TRUE
					&& userIsPrincipalOfACE(user, ace)) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
	
	/**
	 * 
	 * Helper for canWrite() that throws an unauthorized exception
	 * 
	 * @param user
	 * @param entity
	 */
	public void assertCanWrite(User user, Collection entity) {
		if (!canWrite(user, entity)) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}

	/**
	 * Checks if the provided User is a principal of the provided ACE True if
	 * the User is the Principal of the ACE or if the user belongs to the Group
	 * that is the Principal of the ACE
	 * 
	 * @param user
	 * @param ace
	 * @return
	 */
	private boolean userIsPrincipalOfACE(User user, AccessControlEntry ace) {
		Principal principal = ace.getPrincipal();
		if (principal instanceof User) {
			if (user.equals(principal)) {
				return Boolean.TRUE;
			}
		} else if (principal instanceof Group) {
			// TODO - replace this with List<Group> in User
			List<String> userGroupsIds = user.getGroups();
			for (String groupId : userGroupsIds) {
				Group group = groupRepository.findOne(groupId);
				if (group != null) {
					if (group.equals(principal)) {
						return true;
					}
				}
			}
		}
		return Boolean.FALSE;
	}
	
	/**
	 * Checks if user is SUPER ADMIN
	 */
	private boolean isSuperAdmin(User user){
		boolean isSuperAdmin = false;
		for(Role role : user.getRoles()){
			if(role.getRole().equals(Constants.ROLE_SUPER_ADMIN)){
				isSuperAdmin = true;
				break;
			}
		}
		return isSuperAdmin;
	}
	
}
