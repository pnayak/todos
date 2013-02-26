package com.pragyasystems.knowledgeHub.health;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.yammer.metrics.core.HealthCheck.Result;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
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
		ElasticSearchIndex<LearningContent> mockContentIndex = mock(ElasticSearchIndex.class);

		ElasticSearchHealthCheck esHealthCheck = new ElasticSearchHealthCheck(
				mockContentIndex);

		// Healthy Server
		when(mockContentIndex.count(anyString(), anyObject())).thenReturn(4l);
		try {
			assertEquals(esHealthCheck.check(), Result.healthy());
		} catch (Exception e) {
			fail("Should not have thrown an Exception");
			e.printStackTrace();
		}

		// Un-Healthy Server
		when(mockContentIndex.count(anyString(), anyObject())).thenReturn(-1l);
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
