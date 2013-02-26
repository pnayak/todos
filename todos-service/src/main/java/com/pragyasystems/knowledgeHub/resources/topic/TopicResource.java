/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.topic;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.foobar.todos.db.EntityRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pragyasystems.knowledgeHub.api.Collection;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.DeliveryContext;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.api.topic.LearningTopic;
import com.pragyasystems.knowledgeHub.api.topic.TopicTemplate;
import com.pragyasystems.knowledgeHub.db.space.DeliveryContextRepository;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.topic.TopicRepository;
import com.pragyasystems.knowledgeHub.db.topic.TopicTemplateRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResource;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The resource that provides access to learning topics
 * 
 */
@Component
@Path("/learningtopic")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicResource extends BaseResource<LearningTopic<LearningContent>> {

	private static final Logger LOG = LoggerFactory.getLogger(TopicResource.class);
	private TopicRepository topicRepository;
	private ElasticSearchIndex<LearningTopic<LearningContent>> topicIndex;
	private TopicTemplateRepository templateRepository;
	private EntityRepository contentRepository;
	private SpaceRepository spaceRepository;
	private DeliveryContextRepository deliveryContextRepository;

	/**
	 * @param contentRepository
	 */
	@Autowired
	public TopicResource(
			TopicRepository topicRepository,
			ElasticSearchIndex<LearningTopic<LearningContent>> elasticSearchIndexForTopic,
			TopicTemplateRepository templateRepository,
			EntityRepository contentRepository,
			SpaceRepository spaceRepository,
			DeliveryContextRepository deliveryContextRepository) {
		super(topicRepository, elasticSearchIndexForTopic);
		this.topicRepository = topicRepository;
		this.topicIndex = elasticSearchIndexForTopic;
		this.templateRepository = templateRepository;
		this.contentRepository = contentRepository;
		this.spaceRepository = spaceRepository;
		this.deliveryContextRepository = deliveryContextRepository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pragyasystems.knowledgeHub.resources.BaseResource#findAll(com.
	 * pragyasystems.knowledgeHub.api.security.User, java.lang.String,
	 * com.yammer.dropwizard.jersey.params.IntParam,
	 * com.yammer.dropwizard.jersey.params.IntParam)
	 */
	@Override
	@GET
	@Timed
	public Iterable<LearningTopic<LearningContent>> findAll(
			@Auth User user,
			@QueryParam("parentSpaceId") @DefaultValue("NONE") String parentSpaceId,
			@QueryParam("page") @DefaultValue("0") IntParam page,
			@QueryParam("count") @DefaultValue("20") IntParam count) {

		Iterable<LearningTopic<LearningContent>> topicList = null;
		Pageable pageable = new PageRequest(page.get(), count.get());

		if (parentSpaceId != "NONE") {
			LearningSpace parentLearningSpace = spaceRepository
					.findOne(parentSpaceId);
			if (parentLearningSpace == null) {
				// we found an invalid reference to a LearningSpace
				LOG.error("Invalid Parent LearningSpace ID was provided");
				throw new WebApplicationException(Status.BAD_REQUEST);
			}
			topicList = topicRepository.findByAncestor(parentLearningSpace,
					pageable);
			LOG.debug("For space " + parentSpaceId + " found topics "
					+ topicList);
		} else {
			// We will not return anything if parentLearningSpace is not
			// provided
			// TODO - maybe we can determine a sensible default value for
			// parentSpaceId
			// contentList = contentRepository.findAll(pageable);
		}

		if (topicList != null) {
			return topicList;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
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
			LearningTopic<LearningContent> providedTopic) {

		if (parentSpaceId == null) {
			// TODO: default it to the users private learning space Id
			LOG.error("Parent LearningSpace ID was not provided");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// Retrieve LearningSpace corresponding to parentSpaceId to ensure
		// validity and
		// set it on the providedTopic using setAncestor(...)
		LearningSpace parentLearningSpace = spaceRepository
				.findOne(parentSpaceId);
		if (parentLearningSpace == null) {
			// we found an invalid reference to a LearningSpace
			LOG.error("Invalid Parent LearningSpace ID was provided");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		providedTopic.setAncestor(parentLearningSpace);

		// Validate any LearningContent DBRefs are all valid
		List<Collection> contentList = ImmutableList.copyOf(providedTopic
				.getChildren());
		for (Collection entity : contentList) {
			LearningContent content = (LearningContent) entity;
			if (contentRepository.findOne(content.getUuid()) == null) {
				// we found an invalid reference to a LearningContent
				LOG.error("LearningContent reference provided does NOT EXIST...");
				throw new WebApplicationException(Status.BAD_REQUEST);
			} else {
				providedTopic.addChild(content);
			}
		}

		// Ensure that the TopicTemplate is valid
		String templateId = providedTopic.getTemplate().getUuid();

		TopicTemplate template = templateRepository.findOne(templateId);

		if (template == null) {
			LOG.error("Invalid Template ID was provided");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		providedTopic.setTemplate(template);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.pragyasystems.knowledgeHub.resources.BaseResource#beforeUpdate(com
	 * .pragyasystems.knowledgeHub.api.security.User, java.lang.String,
	 * com.pragyasystems.knowledgeHub.api.Entity,
	 * com.pragyasystems.knowledgeHub.api.Entity)
	 */
	@Override
	protected void beforeUpdate(User user, String parentSpaceId,
			LearningTopic<LearningContent> existingTopic,
			LearningTopic<LearningContent> providedTopic) {

		LOG.debug("Trying to update Topic " + providedTopic);

		if (parentSpaceId == null) {
			// TODO: default it to the users private learning space Id
			LOG.error("Parent LearningSpace ID was not provided");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// Retrieve LearningSpace corresponding to parentSpaceId to ensure
		// validity and
		// set it on the providedTopic using setAncestor(...)
		LearningSpace parentLearningSpace = spaceRepository
				.findOne(parentSpaceId);
		if (parentLearningSpace == null) {
			// we found an invalid reference to a LearningSpace
			LOG.error("Invalid Parent LearningSpace ID was provided");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		providedTopic.setAncestor(parentLearningSpace);

		// Validate any LearningContent DBRefs are all valid
		List<Collection> contentList = ImmutableList.copyOf(providedTopic
				.getChildren());
		for (Collection entity : contentList) {
			LearningContent content = (LearningContent) entity;
			if (contentRepository.findOne(content.getUuid()) == null) {
				// we found an invalid reference to a LearningContent
				LOG.error("LearningContent reference provided does NOT EXIST...");
				throw new WebApplicationException(Status.BAD_REQUEST);
			} else {
				providedTopic.addChild(content);
			}
		}

		// Check and set the level map
		Map<String, List<LearningContent>> providedLevelMap = providedTopic
				.getLevelMap();
		LOG.debug("Provided levelMap = " + providedLevelMap);
		Map<String, List<LearningContent>> actualLevelMap = Maps.newHashMap();
		List<LearningContent> actualContents;

		Iterator<String> levelUuids = providedLevelMap.keySet().iterator();
		while (levelUuids.hasNext()) {
			String levelUuid = levelUuids.next();
			Iterable<LearningContent> contents = providedLevelMap
					.get(levelUuid);
			actualContents = Lists.newArrayList();
			for (LearningContent content : contents) {
				LearningContent actualContent = contentRepository
						.findOne(content.getUuid());
				if (actualContent == null) {
					// we found an invalid reference to a LearningContent
					LOG.error("LearningContent reference provided does NOT EXIST...");
					throw new WebApplicationException(Status.BAD_REQUEST);
				} else {
					actualContents.add(actualContent);
					// barList.add(actualContent);
				}
			}
			LOG.debug("Actual contents list = " + actualContents);
			actualLevelMap.put(levelUuid, actualContents);
		}
		LOG.debug("Actual levelMap = " + actualLevelMap);
		providedTopic.setLevelMap(actualLevelMap);

		// Ensure that the TopicTemplate is valid
		String templateId = providedTopic.getTemplate().getUuid();
		if (!templateId.equals(existingTopic.getTemplate().getUuid())) {
			// TODO - should we allow update of template on an existing topic?
			LOG.error("Attempt to change templates on existing LearningTopic - this is NOT allowed..");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		TopicTemplate template = templateRepository.findOne(templateId);

		if (template == null) {
			LOG.error("Invalid Template ID was provided");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		providedTopic.setTemplate(template);
	}

	@PUT
	@Path("/{uuid}/template/{templateId}")
	@Timed
	public Response setTemplate(@PathParam("uuid") String uuid,
			@PathParam("templateId") String templateId) {

		LearningTopic<LearningContent> topic = topicRepository.findOne(uuid);

		if (topic == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		TopicTemplate template = templateRepository.findOne(templateId);

		if (template == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		topic.setTemplate(template);
		topicRepository.save(topic);
		topicIndex.addToIndex(topic);

		LOG.debug("Added Template to Topic");

		return Response
				.created(UriBuilder.fromPath("/{uuid}").build(topic.getUuid()))
				.entity(topic).build();
	}

	@PUT
	@Path("/{uuid}/updatetopiclist")
	@Timed
	public Response updateTopicList(@PathParam("uuid") String uuid,
			@Valid DeliveryContext providedDeliveryContext) {

		DeliveryContext deliveryContext = (DeliveryContext) deliveryContextRepository
				.findOne(uuid);

		if (deliveryContext != null) {

			// Get existing topic in DB, assume for now that DC can have max 100
			// topics.
			Pageable pageable = new PageRequest(0, 100);
			List<LearningTopic<LearningContent>> existingTopicList = topicRepository
					.findByAncestor(deliveryContext, pageable);

			/* updated Topic list from Form */
			List<LearningTopic<LearningContent>> updatedTopicList = providedDeliveryContext
					.getTopicList();

			deleteRemovedTopics(existingTopicList, updatedTopicList);

			copyNewTopics(existingTopicList, updatedTopicList, deliveryContext);

		} else {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		return Response
				.created(
						UriBuilder.fromPath("/{uuid}").build(
								deliveryContext.getUuid()))
				.entity(deliveryContext).build();
	}

	/**
	 * Copy the topic if not in the existing topic list.
	 * 
	 * @param existingTopicList
	 * @param updatedTopicList
	 */
	private void copyNewTopics(
			List<LearningTopic<LearningContent>> existingTopicList,
			List<LearningTopic<LearningContent>> updatedTopicList,
			DeliveryContext parentDeliveryContext) {

		for (LearningTopic<LearningContent> newTopic : updatedTopicList) {
			boolean add = true;
			// Check if topic in the updated list.
			for (LearningTopic<LearningContent> existingTopic : existingTopicList) {
				if (existingTopic.getUuid().equals(newTopic.getUuid())) {
					add = false;
					break;
				}
			}
			if (add) {
				LearningTopic<LearningContent> copyTopic = new LearningTopic<LearningContent>();
				LearningTopic<LearningContent> sourceTopic = topicRepository
						.findOne(newTopic.getUuid());
				copyTopic.setTitle(sourceTopic.getTitle());
				copyTopic.setTemplate(sourceTopic.getTemplate());
				copyTopic.setDescription(sourceTopic.getDescription());
				copyTopic.setAncestor(parentDeliveryContext);
				copyTopic.setLevelMap(sourceTopic.getLevelMap());
				// TODO For now set the same LearningContents (proxy), need
				// to copy LC too
				copyTopic.setChildren(sourceTopic.getChildren());
				topicRepository.save(copyTopic);
			}
		}
	}

	/**
	 * Delete the topic if not in the updated topic list.
	 * 
	 * @param existingTopicList
	 * @param updatedTopicList
	 */
	private void deleteRemovedTopics(
			List<LearningTopic<LearningContent>> existingTopicList,
			List<LearningTopic<LearningContent>> updatedTopicList) {

		for (LearningTopic<LearningContent> existingTopic : existingTopicList) {
			boolean delete = true;
			// Check if topic in the updated list.
			for (LearningTopic<LearningContent> newTopic : updatedTopicList) {
				if (existingTopic.getUuid().equals(newTopic.getUuid())) {
					delete = false;
					break;
				}
			}
			if (delete) {
				topicRepository.delete(existingTopic);
			}
		}
	}
}
