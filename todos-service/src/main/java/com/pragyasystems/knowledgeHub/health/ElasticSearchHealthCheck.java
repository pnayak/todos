/**
 * 
 */
package com.pragyasystems.knowledgeHub.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.yammer.metrics.core.HealthCheck;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         A health checker for ElasticSearch
 * 
 */
@Component
public class ElasticSearchHealthCheck extends HealthCheck {

	private ElasticSearchIndex<LearningContent> elasticSearchIndex;

	@Autowired
	protected ElasticSearchHealthCheck(
			ElasticSearchIndex<LearningContent> elasticSearchIndexForContent) {
		super("ElasticSearch Health Check");
		this.elasticSearchIndex = elasticSearchIndexForContent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yammer.metrics.core.HealthCheck#check()
	 */
	@Override
	protected Result check() throws Exception {
		if (elasticSearchIndex.count("title", "quiz") >= 0) {
			return Result.healthy();
		}
		return Result
				.unhealthy("Unable to verify that the ElasticSearch is running and accessible");
	}
}
