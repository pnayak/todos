/**
 * 
 */
package com.foobar.todos.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.api.security.User;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.yammer.metrics.core.HealthCheck;

/**
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         A health checker for ElasticSearch
 * 
 */
@Component
public class ElasticSearchHealthCheck extends HealthCheck {

	private ElasticSearchIndex<User> elasticSearchIndex;

	@Autowired
	protected ElasticSearchHealthCheck(
			ElasticSearchIndex<User> elasticSearchIndexForUser) {
		super("ElasticSearch Health Check");
		this.elasticSearchIndex = elasticSearchIndexForUser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yammer.metrics.core.HealthCheck#check()
	 */
	@Override
	protected Result check() throws Exception {
		if (elasticSearchIndex.count("title", "task") >= 0) {
			return Result.healthy();
		}
		return Result
				.unhealthy("Unable to verify that the ElasticSearch is running and accessible");
	}
}
