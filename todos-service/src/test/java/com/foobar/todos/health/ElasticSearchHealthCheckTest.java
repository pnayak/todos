package com.foobar.todos.health;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.foobar.todos.api.security.User;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.yammer.metrics.core.HealthCheck.Result;

/**
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Test the ElasticSearchHealthCheck
 */
@Test(groups = { "checkintest" })
public class ElasticSearchHealthCheckTest {

	@Test
	public void checkTest() {
		@SuppressWarnings("unchecked")
		ElasticSearchIndex<User> mockUserIndex = mock(ElasticSearchIndex.class);

		ElasticSearchHealthCheck esHealthCheck = new ElasticSearchHealthCheck(
				mockUserIndex);

		// Healthy Server
		when(mockUserIndex.count(anyString(), anyObject())).thenReturn(4l);
		try {
			assertEquals(esHealthCheck.check(), Result.healthy());
		} catch (Exception e) {
			fail("Should not have thrown an Exception");
			e.printStackTrace();
		}

		// Un-Healthy Server
		when(mockUserIndex.count(anyString(), anyObject())).thenReturn(-1l);
		try {
			assertEquals(
					esHealthCheck.check(),
					Result.unhealthy("Unable to verify that the ElasticSearch is running and accessible"));
		} catch (Exception e) {
			fail("Should not have thrown an Exception");
			e.printStackTrace();
		}
	}
}
