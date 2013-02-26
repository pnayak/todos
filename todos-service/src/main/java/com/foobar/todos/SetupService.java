package com.foobar.todos;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.foobar.todos.constants.Constants;
import com.pragyasystems.knowledgeHub.api.security.AccessControlEntry;
import com.pragyasystems.knowledgeHub.api.security.Group;
import com.pragyasystems.knowledgeHub.api.security.Role;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.group.GroupRepository;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.space.LearningSpaceUtil;
import com.pragyasystems.knowledgeHub.resources.user.UserUtils;
import com.pragyasystems.knowledgeHub.security.AuthUtil;
import com.yammer.dropwizard.lifecycle.Managed;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Deepak Lalwani
 * 
 *   Create root learning space, superadmin user on server startup. 
 */

public class SetupService implements Managed {

	private static final Logger LOG =  LoggerFactory
			.getLogger(SetupService.class);

	@Autowired
	private SpaceRepository spaceRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private GroupRepository groupRepository;
	@Autowired
	private LearningSpaceUtil learningSpaceUtil;
	@Autowired
	private AuthUtil authZ;


	public SetupService() {

	}

	public SetupService(SpaceRepository spaceRepository,
			UserRepository userRepository, GroupRepository groupRepository, LearningSpaceUtil learningSpaceUtil) {
		this.spaceRepository = spaceRepository;
		this.userRepository = userRepository;
		this.groupRepository = groupRepository;
		this.learningSpaceUtil = learningSpaceUtil;
	}

	@Override
	public void start() throws Exception {
		doSetup();
	}

	@Override
	public void stop() throws Exception {
		// Do nothing ....
	}

	private void doSetup() throws Exception {
		
		List<LearningSpace> spaces = spaceRepository
				.findByTitle(Constants.ROOT_LEARNING_SPACE_TITLE);
		
		// Populate roles 
		populateRoles();
		
		if (spaces == null || spaces.size() == 0) {
			
			LOG.info("******* Creating Super Admin User *******");
			User superAdmin = new User();
			superAdmin.setFirstName("SUPER");
			superAdmin.setLastName("ADMIN");
			superAdmin.setUsername("superAdmin");
			superAdmin.setPassword(UserUtils.hashPassword("Pragya2012"));
			Role superAdminRole = authZ.SUPERADMIN;
			superAdmin.addRole(superAdminRole);			
			superAdmin = userRepository.save(superAdmin);

			LOG.info("******* Creating SUPER_ADMIN Group *******");
			Group group = new Group();
			group.setName("SUPER_ADMIN");
			group.addUser(superAdmin);
			group = groupRepository.save(group);
			superAdmin.addGroup(group);
			superAdmin = userRepository.save(superAdmin);
			AccessControlEntry ace = new AccessControlEntry();
			ace.setPrincipal(group);
			ace.setRead(Boolean.TRUE);
			ace.setWrite(Boolean.TRUE);
			
			LOG.info("******* Creating "
					+ Constants.ROOT_LEARNING_SPACE_TITLE + " *******");
			LearningSpace root = new LearningSpace();
			root.setTitle(Constants.ROOT_LEARNING_SPACE_TITLE);
			root.setDescription("Pragya Systems Root Learning Space");
			root.setCategory(Constants.LS_CATEGORY_LEARNING_SPACE);
			root.addACE(ace);
			spaceRepository.save(root);
			createPragyaLearningSpace(root);
			
		} else {
			LOG.info("******* " + Constants.ROOT_LEARNING_SPACE_TITLE
					+ " Already exists...*******");
		}

	}
	
	private void createPragyaLearningSpace(LearningSpace root){
		LOG.info("******* Creating Pragya Public Learning Space *******");
		LearningSpace pragya = new LearningSpace();
		pragya.setTitle(Constants.PRAGYA_PUBLIC_LEARNING_SPACE_TITLE);
		pragya.setDescription("Pragya Public Learning Space");
		pragya.setCategory(Constants.LS_CATEGORY_LEARNING_SPACE);
		pragya.setAncestor(root);
		learningSpaceUtil.createGroupsForAllRoles(pragya);
	}
	
	private void populateRoles() {
		
		//TODO - save to database
		// NOTE: We explicitly set the UUID so it is a constant
		
		// SuperAdmin role
		Role superAdminRole = new Role();
		superAdminRole.setUuid("superAdminRole-42-2012");
		superAdminRole.setRole(Constants.ROLE_SUPER_ADMIN);
		authZ.SUPERADMIN = superAdminRole;
		
		// Institution Admin role
		Role adminRole = new Role();
		adminRole.setUuid("adminRole-42-2012");
		adminRole.setRole(Constants.ROLE_ADMIN);
		authZ.ADMIN = adminRole;
		
		// Instructor role
		Role instructorRole = new Role();
		instructorRole.setUuid("instructorRole-42-2012");
		instructorRole.setRole(Constants.ROLE_INSTRUCTOR);
		authZ.INSTRUCTOR = instructorRole;
		
		// Student role
		Role studentRole = new Role();
		studentRole.setUuid("studentRole-42-2012");
		studentRole.setRole(Constants.ROLE_STUDENT);
		authZ.STUDENT = studentRole;
	}
}