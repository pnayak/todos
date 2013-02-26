package com.foobar.todos.elasticsearch;

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

import com.foobar.todos.api.security.User;

/**
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

		when(
				mockElasticSearchClient.prepareIndex(anyString(), anyString(),
						anyString())).thenReturn(mockIndexRequestBuilder);

		when(mockIndexRequestBuilder.setSource(anyMap())).thenReturn(
				mockIndexRequestBuilder);

		when(mockIndexRequestBuilder.execute()).thenReturn(
				mockListenableActionFuture);
		when(mockListenableActionFuture.actionGet()).thenReturn(
				mock(IndexResponse.class));

		ElasticSearchIndex<User> userIndex = new ElasticSearchIndex<User>(
				User.class, mockElasticSearchClient);

		userIndex.setBulkMode(Boolean.FALSE);

		assertEquals(userIndex.getIndexName(), User.class.getSimpleName()
				.toLowerCase());

		User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("1.0");
		user.setUsername("Test");
		user.setPassword("This is a description");

		userIndex.addToIndex(user);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addToIndexInBulkTest() {

		Client mockElasticSearchClient = mock(Client.class);
		IndexRequestBuilder mockIndexRequestBuilder = mock(IndexRequestBuilder.class);
		ListenableActionFuture<IndexResponse> mockListenableActionFuture = mock(ListenableActionFuture.class);

		when(
				mockElasticSearchClient.prepareIndex(anyString(), anyString(),
						anyString())).thenReturn(mockIndexRequestBuilder);

		when(mockIndexRequestBuilder.setSource(anyMap())).thenReturn(
				mockIndexRequestBuilder);

		when(mockIndexRequestBuilder.execute()).thenReturn(
				mockListenableActionFuture);
		when(mockListenableActionFuture.actionGet()).thenReturn(
				mock(IndexResponse.class));
		when(mockIndexRequestBuilder.request()).thenReturn(
				mock(IndexRequest.class));

		ElasticSearchIndex<User> userIndex = new ElasticSearchIndex<User>(
				User.class, mockElasticSearchClient);

		userIndex.setBulkMode(Boolean.TRUE);

		assertEquals(userIndex.getIndexName(), User.class.getSimpleName()
				.toLowerCase());

		User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("1.0");
		user.setUsername("Test");
		user.setPassword("This is a description");

		userIndex.addToIndex(user);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void removeFromIndexTest() {

		Client mockElasticSearchClient = mock(Client.class);
		DeleteRequestBuilder mockDeleteRequestBuilder = mock(DeleteRequestBuilder.class);
		ListenableActionFuture<DeleteResponse> mockListenableActionFuture = mock(ListenableActionFuture.class);

		when(
				mockElasticSearchClient.prepareDelete(anyString(), anyString(),
						anyString())).thenReturn(mockDeleteRequestBuilder);

		when(mockDeleteRequestBuilder.execute()).thenReturn(
				mockListenableActionFuture);
		when(mockListenableActionFuture.actionGet()).thenReturn(
				mock(DeleteResponse.class));

		ElasticSearchIndex<User> userIndex = new ElasticSearchIndex<User>(
				User.class, mockElasticSearchClient);

		User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setVersion("1.0");
		user.setUsername("Test");
		user.setPassword("This is a description");

		userIndex.removeFromIndex(user);
	}
}
