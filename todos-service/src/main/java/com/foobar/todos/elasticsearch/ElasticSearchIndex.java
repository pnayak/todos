/**
 * 
 */
package com.foobar.todos.elasticsearch;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.fieldQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foobar.todos.api.Entity;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         This represents the ElasticSearch index for an {@Entity}
 * 
 */
public class ElasticSearchIndex<T extends Entity> {

	private static final Logger LOG = LoggerFactory.getLogger(ElasticSearchIndex.class);

	private final String type;

	private Client elasticSearchClient;
	private String indexName;

	private boolean bulkMode = Boolean.TRUE; // TODO - set from configuration
	private int poolSize = 2; // TODO - make this configurable
	private long sleepTime = 1 * 1000; // TODO - make this configurable
	private int batchSize = 5000; // TODO - make this configurable
	private BlockingQueue<IndexRequest> indexQueue = new ArrayBlockingQueue<IndexRequest>(
			3000000, true);
	private final ExecutorService indexingService = Executors
			.newFixedThreadPool(poolSize);

	/**
	 * 
	 */
	public ElasticSearchIndex(Class<? extends T> type,
			Client elasticSearchClient) {

		super();

		this.type = type.getSimpleName(); //TODO - does this need to include LearningSpace in it?

		setIndexName(this.type.toLowerCase()); // ElasticSearch is picky about
												// this :(

		this.elasticSearchClient = elasticSearchClient;
		LOG.debug("Initialized ElasticSearch client" + elasticSearchClient);

		createESIndex();

		indexingService.execute(new BackgroundIndexerThread());
		LOG.debug("Started background indexing thread");

	}

	public long count(String key, Object value) {
		CountResponse response = elasticSearchClient
				.prepareCount(this.indexName).setQuery(termQuery(key, value))
				.setTypes(this.type).execute().actionGet();
		long count = response.getCount();
		LOG.debug("Count search returned  " + count);
		return count;
	}

	public String getIndexName() {
		checkNotNull(indexName);
		return this.indexName;
	}

	/**
	 * @param indexName
	 *            the indexName to set
	 */
	public void setIndexName(String indexName) {
		checkNotNull(indexName);
		this.indexName = indexName;
	}

	private void createESIndex() {
		// Nothing to do by default, ElasticSearch automatically creates an
		// index if one does not exist
		// TODO - for near future - create index with specific options if
		// neccessary (like num shards, etc.)
	}

	public void addToIndex(T objToIndex) {

		LOG.debug("addToIndex: trying to index " + objToIndex);

		checkNotNull(indexName);
		checkNotNull(objToIndex.getUuid());
		checkNotNull(this.elasticSearchClient);

		String id = objToIndex.getUuid().toString();
		ObjectMapper mapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		Map<String, Object> props = mapper.convertValue(objToIndex, Map.class);

		IndexRequestBuilder indexRequestBuilder = elasticSearchClient
				.prepareIndex(this.indexName, this.type, id).setSource(props);

		if (this.bulkMode) {
			IndexRequest indexRequest = indexRequestBuilder.request();
			indexQueue.add(indexRequest);
			LOG.debug("added request to indexQueue : "
					+ indexRequest.toString());
		} else {
			try {
				IndexResponse response = indexRequestBuilder.execute()
						.actionGet();
				LOG.debug("addToIndex - result is : " + response.toString());
			} catch (ElasticSearchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void addToIndexBatch(Iterable<IndexRequest> indexRequests) {
		checkNotNull(indexName);
		BulkRequestBuilder bulkIndexRequest = elasticSearchClient.prepareBulk();
		if (Iterables.size(indexRequests) > 0) {
			for (IndexRequest indexRequest : indexRequests) {
				try {
					bulkIndexRequest = bulkIndexRequest.add(indexRequest);
					LOG.debug("addToIndexBatch: added document to bulkIndexRequestx");
				} catch (ElasticSearchException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			LOG.info("addToIndexBatch: There are no documents in the Iterable.  Doing nothing.");
		}
		try {
			long startTime = System.currentTimeMillis();
			BulkResponse response = bulkIndexRequest.execute().actionGet();
			long elapsedTime = System.currentTimeMillis() - startTime;
			LOG.debug("Bulk added to index.  It took : " + elapsedTime / 1000
					+ " seconds");
		} catch (ElasticSearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void removeFromIndex(T objToRemoveFromIndex) {
		checkNotNull(indexName);
		String id = objToRemoveFromIndex.getUuid().toString();
		DeleteResponse response = elasticSearchClient
				.prepareDelete(this.indexName, this.type, id).execute()
				.actionGet();
		LOG.debug("removeFromIndex - result is : " + response.toString());
	}

	/**
	 * Search for a given type within this index, using a SINGLE key/value
	 * 
	 * To search all types, it is OK to pass in a value of type = null
	 * 
	 * @param type
	 *            - the type of this index
	 * @param key
	 *            - the attribute key to search for
	 * @param value
	 *            - the attribute value to search for
	 * @return
	 */
	public Iterable<String> query(String type, String key, String value) {

		Map<String, Object> terms = Maps.newHashMap();
		terms.put(key, value);
		return query(type, terms, 100000);
	}

	/**
	 * Search for a given type within this index, using a Multiple key/value
	 * which in turn creates a BooleanQuery
	 * 
	 * To search all types, it is OK to pass in a value of type = null
	 * 
	 * @param type
	 *            - the type of this index
	 * @param key
	 *            - the attribute key to search for
	 * @param value
	 *            - the attribute value to search for
	 * @return
	 */
	public Iterable<String> query(String type, Map<String, Object> terms,
			int resultSize) {

		BoolQueryBuilder boolQueryBuilder = boolQuery();

		for (String key : terms.keySet()) {
			Object value = terms.get(key);
			boolQueryBuilder.must(fieldQuery(key, value));
		}

		SearchRequestBuilder search = elasticSearchClient
				.prepareSearch(this.indexName)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(boolQueryBuilder).setTypes(type).setFrom(0)
				.setSize(resultSize).setExplain(false);

		LOG.debug("Executing search query: " + search.toString());
		System.out.println("Executing search query: " + search.toString());

		SearchResponse response = search.execute().actionGet();

		Iterator<SearchHit> it = response.getHits().iterator();
		Set<String> ids = Sets.newHashSet();
		while (it.hasNext()) {
			SearchHit hit = it.next();
			ids.add(hit.getId());
			LOG.debug("Search returned id " + hit.getId());
		}
		return ids;
	}

	public void deleteIndex() {
		DeleteIndexResponse delete = elasticSearchClient.admin().indices()
				.delete(new DeleteIndexRequest(this.indexName)).actionGet();
		if (!delete.acknowledged()) {
			LOG.error("deleteIndex: Index " + this.indexName
					+ " wasn't deleted");
		}
	}

	/**
	 * @return the bulkMode
	 */
	public boolean isBulkMode() {
		return bulkMode;
	}

	/**
	 * @param bulkMode
	 *            the bulkMode to set
	 */
	public void setBulkMode(boolean bulkMode) {
		this.bulkMode = bulkMode;
	}

	class BackgroundIndexerThread implements Runnable {
		public void run() {
			while (true) {
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (Iterables.size(indexQueue) > 0) {
					List<IndexRequest> batch = Lists
							.newArrayListWithCapacity(batchSize);
					indexQueue.drainTo(batch, batchSize);
					LOG.debug("BackgroundIndexerThread: About to index "
							+ Iterables.size(batch) + " documents");
					addToIndexBatch(batch); // TODO - what to do if this
											// fails!!! Docs will not index
					LOG.debug("BackgroundIndexerThread: Done! Onto the next batch...");
				}
				Thread.yield();
			}

		}
	}
}
