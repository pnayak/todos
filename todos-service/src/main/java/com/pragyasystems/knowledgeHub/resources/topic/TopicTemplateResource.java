/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.topic;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.google.common.collect.Lists;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.topic.LearningLevel;
import com.pragyasystems.knowledgeHub.api.topic.TopicTemplate;
import com.pragyasystems.knowledgeHub.db.topic.LearningLevelRepository;
import com.pragyasystems.knowledgeHub.db.topic.TopicTemplateRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResource;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The resource that provides access to topic templates
 * 
 */
@Component
@Path("/topictemplate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicTemplateResource extends BaseResource<TopicTemplate> {

	private static final Logger LOG = LoggerFactory.getLogger(TopicTemplateResource.class);
	private LearningLevelRepository levelRepository;

	/**
	 * @param repository
	 * @param index
	 */
	@Autowired
	public TopicTemplateResource(
			TopicTemplateRepository topicTemplateRepository,
			ElasticSearchIndex<TopicTemplate> elasticSearchIndexForTopicTemplate,
			LearningLevelRepository levelRepository) {
		super(topicTemplateRepository, elasticSearchIndexForTopicTemplate);
		this.levelRepository = levelRepository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pragyasystems.knowledgeHub.resources.BaseResource#beforeAdd(com.
	 * pragyasystems.knowledgeHub.api.security.User, java.lang.String,
	 * com.pragyasystems.knowledgeHub.api.Entity)
	 */
	@Override
	protected void beforeAdd(User user, String parentSpaceId,
			TopicTemplate providedTopicTemplate) {

		// Note: TopicTemplates are currently GLOBAL. So we
		// do not check for or validate the parentSpaceId

		List<LearningLevel> levelList = providedTopicTemplate.getLevelList();

		List<LearningLevel> foundLevelList = Lists.newArrayList();
		for (LearningLevel level : levelList) {
			if (level != null & level.getUuid() != null) {
				LearningLevel foundLevel = levelRepository.findOne(level
						.getUuid());
				if (foundLevel != null) {
					foundLevelList.add(foundLevel);
				}
			}
		}

		providedTopicTemplate.setLevelList(foundLevelList);
	}
}
