package com.foobar.todos.health;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.testng.annotations.Test;

import com.foobar.todos.health.MongoHealthCheck;
import com.yammer.metrics.core.HealthCheck.Result;

/**
 * @author Prashant Nayak (pnayak)
 *
 * Test the MongoHealthCheck
 */
@Test(groups = { "checkintest" })
public class MongoHealthCheckTest {

	@Test
	public void checkTest() {
		MongoTemplate mockMongoTemplate = mock(MongoTemplate.class);
		
		// Healthy server check
		when(mockMongoTemplate.collectionExists(anyString())).thenReturn(Boolean.TRUE);
		
		MongoHealthCheck mongoHealthCheck = new MongoHealthCheck(mockMongoTemplate);
		try {
			assertEquals(mongoHealthCheck.check(), Result.healthy());
		} catch (Exception e) {
			fail("Should not have thrown an Exception");
			e.printStackTrace();
		}
		
		// Un-healthy server check
		when(mockMongoTemplate.collectionExists(anyString())).thenReturn(Boolean.FALSE);
		
		mongoHealthCheck = new MongoHealthCheck(mockMongoTemplate);
		try {
			assertEquals(mongoHealthCheck.check(), Result.unhealthy("Unable to verify that the learningContent collection exists"));
		} catch (Exception e) {
			fail("Should not have thrown an Exception");
			e.printStackTrace();
		}
	}
}
