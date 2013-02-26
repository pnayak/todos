package com.pragyasystems.knowledgeHub.elasticsearch;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.testng.annotations.Test;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Test the ElasticSearchIndex helper service
 */
@Test(groups = { "checkintest" })
public class ElasticSearchIndexTest {

	@SuppressWarnings("unchecked")
	@Test
	public void addToIndexTest() {

		Client mockElasticSearchClient = mock(Client.class);
		IndexRequestBuilder mockIndexRequestBuilder = mock(IndexRequestBuilder.class);
		ListenableActionFuture<IndexResponse> mockListenableActionFuture = mock(ListenableActionFuture.class);

		when(mockElasticSearchClient.prepareIndex(anyString(), anyString(),
						anyString())).thenReturn(mockIndexRequestBuilder);

		when(mockIndexRequestBuilder.setSource(anyMap())).thenReturn(
				mockIndexRequestBuilder);
		
		when(mockIndexRequestBuilder.execute()).thenReturn(mockListenableActionFuture);
		when(mockListenableActionFuture.actionGet()).thenReturn(mock(IndexResponse.class));

		ElasticSearchIndex<LearningContent> contentIndex = new ElasticSearchIndex<LearningContent>(
				LearningContent.class, mockElasticSearchClient);

		contentIndex.setBulkMode(Boolean.FALSE);

		assertEquals(contentIndex.getIndexName(), LearningContent.class
				.getSimpleName().toLowerCase());

		LearningContent content = new LearningContent();
		content.setUuid("511846484800016990f86233");
		content.setVersion("1.0");
		content.setTitle("Test");
		content.setDescription("This is a description");

		contentIndex.addToIndex(content);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void addToIndexInBulkTest() {

		Client mockElasticSearchClient = mock(Client.class);
		IndexRequestBuilder mockIndexRequestBuilder = mock(IndexRequestBuilder.class);
		ListenableActionFuture<IndexResponse> mockListenableActionFuture = mock(ListenableActionFuture.class);

		when(mockElasticSearchClient.prepareIndex(anyString(), anyString(),
						anyString())).thenReturn(mockIndexRequestBuilder);

		when(mockIndexRequestBuilder.setSource(anyMap())).thenReturn(
				mockIndexRequestBuilder);
		
		when(mockIndexRequestBuilder.execute()).thenReturn(mockListenableActionFuture);
		when(mockListenableActionFuture.actionGet()).thenReturn(mock(IndexResponse.class));
		when(mockIndexRequestBuilder.request()).thenReturn(mock(IndexRequest.class));

		ElasticSearchIndex<LearningContent> contentIndex = new ElasticSearchIndex<LearningContent>(
				LearningContent.class, mockElasticSearchClient);

		assert contentIndex.isBulkMode(); // True by default

		assertEquals(contentIndex.getIndexName(), LearningContent.class
				.getSimpleName().toLowerCase());

		LearningContent content = new LearningContent();
		content.setUuid("511846484800016990f86233");
		content.setVersion("1.0");
		content.setTitle("Test");
		content.setDescription("This is a description");

		contentIndex.addToIndex(content);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void removeFromIndexTest() {

		Client mockElasticSearchClient = mock(Client.class);
		DeleteRequestBuilder mockDeleteRequestBuilder = mock(DeleteRequestBuilder.class);
		ListenableActionFuture<DeleteResponse> mockListenableActionFuture = mock(ListenableActionFuture.class);

		when(mockElasticSearchClient.prepareDelete(anyString(), anyString(),
						anyString())).thenReturn(mockDeleteRequestBuilder);
		
		when(mockDeleteRequestBuilder.execute()).thenReturn(mockListenableActionFuture);
		when(mockListenableActionFuture.actionGet()).thenReturn(mock(DeleteResponse.class));

		ElasticSearchIndex<LearningContent> contentIndex = new ElasticSearchIndex<LearningContent>(
				LearningContent.class, mockElasticSearchClient);

		LearningContent content = new LearningContent();
		content.setUuid("511846484800016990f86233");
		content.setVersion("1.0");
		content.setTitle("Test");
		content.setDescription("This is a description");

		contentIndex.removeFromIndex(content);
	}
}
