/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.topic;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.topic.LearningLevel;
import com.pragyasystems.knowledgeHub.db.topic.LearningLevelRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResource;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The resource that provides access to learning levels
 * 
 */
@Component
@Path("/learninglevel")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LearningLevelResource extends BaseResource<LearningLevel> {

	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(LearningLevelResource.class);

	/**
	 * @param contentRepository
	 */
	@Autowired
	public LearningLevelResource(LearningLevelRepository levelRepository,
			ElasticSearchIndex<LearningLevel> elasticSearchIndexForLearningLevel) {
		super(levelRepository, elasticSearchIndexForLearningLevel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.pragyasystems.knowledgeHub.resources.BaseResource#beforeFindOne(com
	 * .pragyasystems.knowledgeHub.api.security.User, java.lang.String)
	 */
	@SuppressWarnings("static-access")
	@Override
	protected void beforeFindOne(User user, String uuid) {
		//authZ.assertAnyRole(user, authZ.SUPERADMIN, authZ.ADMIN, authZ.INSTRUCTOR);
	}
}
