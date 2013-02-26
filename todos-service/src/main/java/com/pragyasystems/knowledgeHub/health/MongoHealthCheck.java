/**
 * 
 */
package com.pragyasystems.knowledgeHub.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.yammer.metrics.core.HealthCheck;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Checks the health of MongoDB
 */
@Component
public class MongoHealthCheck extends HealthCheck {

	private MongoTemplate mongoTemplate;

	@Autowired
	protected MongoHealthCheck(MongoTemplate mongoTemplate) {
		super("MongoDB Health Check");
		this.mongoTemplate = mongoTemplate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yammer.metrics.core.HealthCheck#check()
	 */
	@Override
	protected Result check() throws Exception {
		if (mongoTemplate.collectionExists("learningContent")) {
			return Result.healthy();
		}
		return Result
				.unhealthy("Unable to verify that the learningContent collection exists");
	}

}
