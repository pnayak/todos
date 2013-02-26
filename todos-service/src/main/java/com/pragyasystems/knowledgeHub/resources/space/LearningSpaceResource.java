/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.space;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResource;
import com.yammer.metrics.annotation.Timed;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 */
@Component
@Path("/learningspace")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LearningSpaceResource extends BaseResource<LearningSpace> {

	private static final Logger LOG = LoggerFactory.getLogger(LearningSpaceResource.class);
	private LearningSpaceUtil learningSpaceUtil;

	@Autowired
	public LearningSpaceResource(SpaceRepository spaceRepository, 
			ElasticSearchIndex<LearningSpace> elasticSearchIndexForSpace,
			LearningSpaceUtil learningSpaceUtil) {
		super(spaceRepository, elasticSearchIndexForSpace);
		this.learningSpaceUtil = learningSpaceUtil;
	}

	@GET
	@Timed
	@Path("/user")
	public Iterable<LearningSpace> getEnrolledLearningSpaceForUser(@QueryParam("userUuid") @DefaultValue("NONE") String userUuid) {
		return learningSpaceUtil.getEnrolledLearningSpaceForUser(userUuid);
	}
}
