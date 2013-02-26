package com.foobar.todos.api;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.foobar.todos.api.Entity;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Tests for Entity
 */
@Test(groups = { "checkintest" })
public class EntityTest {

	@Test
	public void getCreationTime() {

		Entity entity = new Entity();
		long creationTime;

		// Without a UUID set, we should expect 0l
		creationTime = entity.getCreationTime();
		assertEquals(creationTime, 0l);

		// Set a valid MongoDB Id... now we expect a valid creation time in
		// milliseconds
		entity.setUuid("511846484800016990f86233");
		creationTime = entity.getCreationTime();
		assertEquals(creationTime, 1360545352000l);
	}
}
