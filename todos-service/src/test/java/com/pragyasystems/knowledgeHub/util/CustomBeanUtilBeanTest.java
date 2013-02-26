package com.pragyasystems.knowledgeHub.util;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.foobar.todos.api.Entity;
import com.pragyasystems.knowledgeHub.api.Collection;
import com.pragyasystems.knowledgeHub.api.security.AccessControlEntry;
import com.pragyasystems.knowledgeHub.api.security.Group;
import com.pragyasystems.knowledgeHub.api.security.Principal;
import com.pragyasystems.knowledgeHub.api.security.User;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Deepak Lalwani (deepaklalwani)
 * 
 *         Test the CustomBeanUtilBean, specifically copy attributes,
 */
@Test(groups = { "checkintest" })
public class CustomBeanUtilBeanTest {
	
	final CustomBeanUtilBean util = new CustomBeanUtilBean();
	
	@Test
	public void copyProperties() throws Exception{
		
		Collection existingEntity = new Collection();
		existingEntity.setUuid("511846484800016990f86233");
		existingEntity.setTitle("Org-Title");
		existingEntity.setDescription("Test Desc");
		existingEntity.setOwner(getTestUser());
		existingEntity.addACE(getACE(Boolean.TRUE, Boolean.FALSE, "STUDENT_GROUP"));

		Collection updatedEntity = new Collection();
		updatedEntity.setUuid("511846484800016990f86233");
		updatedEntity.setTitle("New-Title");
		
		util.copyProperties(existingEntity, updatedEntity);
		// Test copy not null attributes
		assertEquals(existingEntity.getTitle(), updatedEntity.getTitle());
		assertEquals(existingEntity.getDescription(), "Test Desc");
		assertEquals(existingEntity.getACEList().size(), 1);
		assertEquals(existingEntity.getOwner().getUuid(), "511846484800016990f86233");
		
	}
	
	@Test
	public void copyProperties_testObjectUpdate() throws Exception{
		
		Collection existingEntity = new Collection();
		existingEntity.setUuid("511846484800016990f86233");
		existingEntity.setTitle("Org-Title");
		existingEntity.setDescription("Test Desc");
		existingEntity.setOwner(getTestUser());
		existingEntity.addACE(getACE(Boolean.TRUE, Boolean.FALSE, "STUDENT_GROUP"));

		Collection updatedEntity = new Collection();
		updatedEntity.setUuid("511846484800016990f86233");
		updatedEntity.addACE(getACE(Boolean.TRUE, Boolean.TRUE, "INSTUCTOR_GROUP"));
		
		util.copyProperties(existingEntity, updatedEntity);
		assertEquals(existingEntity.getTitle(), "Org-Title");
		assertEquals(existingEntity.getDescription(), "Test Desc");
		assertEquals(existingEntity.getACEList().size(), 1);
		assertEquals(existingEntity.getACEList().get(0).principal.getName(), "INSTUCTOR_GROUP");
		assertTrue(existingEntity.getACEList().get(0).canRead());
		assertTrue(existingEntity.getACEList().get(0).canWrite());
		
	}

	
	private User getTestUser(){
		User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("A-FAKE-VERSION");
		user.setUsername("jsmith");
		user.setPassword("foobar");
		user.setFirstName("Joe");
		user.setLastName("Smith");
		return user;
	}
	
	private AccessControlEntry getACE(Boolean read, Boolean write, String groupName){
		Group group = new Group();
		group.setName(groupName);
		AccessControlEntry ace = new AccessControlEntry();
		ace.setPrincipal(group);
		ace.setRead(read);
		ace.setWrite(write);
		return ace;
	}
}
