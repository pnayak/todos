/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.content;

import java.util.ArrayList;
import java.util.List;

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
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.foobar.todos.constants.Constants;
import com.foobar.todos.db.EntityRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResource;
import com.pragyasystems.knowledgeHub.resources.space.LearningSpaceUtil;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The resource that provides access to learning content
 * 
 */
@Component
@Path("/learningcontent")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ContentResource extends BaseResource<LearningContent> {

	private static final Logger LOG = LoggerFactory.getLogger(ContentResource.class);
	private EntityRepository contentRepository;
	private SpaceRepository spaceRepository;
	private ElasticSearchIndex<LearningContent> contentIndex;
	@Autowired
	LearningSpaceUtil learningSpaceUtil;

	/**
	 * @param contentRepository
	 */
	@Autowired
	public ContentResource(EntityRepository contentRepository,
			ElasticSearchIndex<LearningContent> elasticSearchIndexForContent,
			UserRepository userRepository,
			SpaceRepository spaceRepository) {
		super(contentRepository, elasticSearchIndexForContent);
		this.contentRepository = contentRepository;
		this.spaceRepository = spaceRepository;
		this.contentIndex = elasticSearchIndexForContent;
	}

	@GET
	@Timed
	@Path("/available")
	public Iterable<LearningContent> findAll(@Auth User user){
		Iterable<LearningSpace> spaces = learningSpaceUtil.getEnrolledLearningSpaceForUser(user.getUuid());
		List<String> ids = new ArrayList<String>();
		for(LearningSpace space : spaces){
			if(space.getCategory().equals(Constants.LS_CATEGORY_LEARNING_SPACE) ||
					space.getCategory().equals(Constants.LS_CATEGORY_USER_PRIVATE) ||
					space.getCategory().equals(Constants.LS_CATEGORY_INSTITUTION_PUBLIC)){
				LOG.debug(">>>>>>>>>>>>>>>>>>"+space.getTitle());
				ids.add(space.getUuid());
			}
		}
		return contentRepository.findByOwnerLearningSpaceUUIDIn(ids.toArray(new String[ids.size()]));
	}
/* Deepak TODO : Remove above method and use this one.
	@GET
	@Timed
	@Path("/available")
	public Iterable<LearningContent> findAll(@Auth User user){
		Iterable<LearningSpace> spaces = learningSpaceUtil.getEnrolledLearningSpaceForUser(user.getUuid());
		List<LearningSpace> spaceList = new ArrayList<LearningSpace>();
		for(LearningSpace space : spaces){
			if(space.getCategory().equals(Constants.LS_CATEGORY_LEARNING_SPACE) ||
					space.getCategory().equals(Constants.LS_CATEGORY_USER_PRIVATE) ||
					space.getCategory().equals(Constants.LS_CATEGORY_INSTITUTION_PUBLIC)){
				spaceList.add(space);
			}
		}
		return contentRepository.findByAncestorIn(spaceList.toArray(new LearningSpace[spaceList.size()]));
	}
*/	
	@GET
	@Timed
	public Iterable<LearningContent> findAll(@Auth User user,
			@QueryParam("parentSpaceId") @DefaultValue("NONE") String parentSpaceId,
			@QueryParam("page") @DefaultValue("0") IntParam page,
			@QueryParam("count") @DefaultValue("20") IntParam count) {

		Iterable<LearningContent> contentList = null;
		Pageable pageable = new PageRequest(page.get(), count.get());

		if(parentSpaceId != "NONE") {
			LearningSpace parentSpace = spaceRepository.findOne(parentSpaceId);
			contentList = contentRepository.findByAncestor(parentSpace);
		} else {
			// We will not return anything if parentLearningSpace is not provided
			// TODO - maybe we can determine a sensible default value for parentSpaceId
			// contentList = contentRepository.findAll(pageable);	
		}

		if (contentList != null) {
			return contentList;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}
/* Deepak TODO : remove above method and use this.
	@Override
	@GET
	@Timed
	public Iterable<LearningContent> findAll(@Auth User user,
			@QueryParam("parentSpaceId") @DefaultValue("NONE") String parentSpaceId,
			@QueryParam("page") @DefaultValue("0") IntParam page,
			@QueryParam("count") @DefaultValue("20") IntParam count) {

		Iterable<LearningContent> contentList = null;
		if(parentSpaceId != "NONE") {
			LearningSpace parentSpace = spaceRepository.findOne(parentSpaceId);
			contentList = contentRepository.findByAncestor(parentSpace);
		} else {
			// We will not return anything if parentLearningSpace is not provided
			// TODO - maybe we can determine a sensible default value for parentSpaceId
			// contentList = contentRepository.findAll(pageable);	
		}

		if (contentList != null) {
			return contentList;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}
*/
	@PUT
	@Path("/incrementWatchCount/{uuid}")
	@Timed
	public LearningContent findWatchCount(@PathParam("uuid") String uuid) {

		if (uuid == null) {
			throw new WebApplicationException(Status.NO_CONTENT);
		}

		LearningContent foundContent = contentRepository.findOne(uuid
				.toString());
		LOG.debug("Incrementing count for object : " + uuid + "by 1");
		foundContent.setWatchCount(foundContent.getWatchCount() + 1);

		LearningContent content = contentRepository.save(foundContent);
		contentIndex.addToIndex(content);

		if (content != null) {
			return content;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}
	
	@Override
	protected void beforeAdd(User user, String parentId,
			LearningContent providedEntity) {
		// set the parent
		LearningSpace parentSpace = spaceRepository.findOne(parentId);
		if (parentSpace == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		// Permission Check if user have write access in parent
		authZ.assertCanWrite(user, parentSpace);
		providedEntity.setAncestor(parentSpace);
		// set the owner
		providedEntity.setOwner(user);
	}
}
