/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.measure;

import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.constants.Constants;
import com.foobar.todos.db.EntityRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.Collection;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.pragyasystems.knowledgeHub.api.measure.UserFeedback;
import com.pragyasystems.knowledgeHub.api.measure.UserFeedback.EnumLike;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.topic.LearningTopic;
import com.pragyasystems.knowledgeHub.db.measure.UserFeedbackRepository;
import com.pragyasystems.knowledgeHub.db.topic.TopicRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResource;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The resource that provides access to learning levels
 * 
 */
@Component
@Path("/userfeedback")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserFeedbackResource extends BaseResource<UserFeedback> {
	
	private static final Logger LOG = LoggerFactory.getLogger(UserFeedbackResource.class);

	private UserFeedbackRepository feedbackRepository;
	
	private UserRepository userRepository;
	
	private EntityRepository contentRepository;
	
	private TopicRepository topicRepository;
	
	/**
	 * @param contentRepository
	 */
	@Autowired
	public UserFeedbackResource(UserFeedbackRepository feedbackRepository,
			ElasticSearchIndex<UserFeedback> elasticSearchIndexForUserFeedback,
			UserRepository userRepository, EntityRepository contentRepository,
			TopicRepository topicRepository) {
		super(feedbackRepository, elasticSearchIndexForUserFeedback);
		this.feedbackRepository = feedbackRepository;
		this.userRepository = userRepository;
		this.contentRepository = contentRepository;
		this.topicRepository = topicRepository;
	}
	

	/**
	 * 	User and learning object are initially received with UUID only to minimize unused data exchange between client and server.
	 *	This method populates the received feedback JSON with user and learning object.
	 * 
	 */
	public void beforeAdd(User user, String parentSpaceId, UserFeedback providedEntity) {
		
		if (providedEntity == null || providedEntity.getUser() == null || providedEntity.getUser().getUuid() == null 
				|| providedEntity.getLearningObject() == null || providedEntity.getLearningObject().getUuid() == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		User providedUser = userRepository.findOne(providedEntity.getUser().getUuid());
		if(providedUser == null) {
			LOG.debug("Invalid user UUID provided.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		} else providedEntity.setUser(providedUser);
		
		//	TODO	--	Here the liked content is assumed to be learning content. Need to support this for other collection objects.
		Collection learningObject = findLearningObjectForIdAndType(providedEntity.getLearningObject().getUuid(), 
				Constants.LO_TYPE_CONTENT);
		if(learningObject == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		} else providedEntity.setLearningObject(learningObject);		
	}
	
	@GET
	@Path("/count")
	@Timed
	public HashMap<String, Integer> findContentFeedbackForInstructor(
			@QueryParam("learningObjectId") @DefaultValue("NONE") String learningObjectId) {
		
		if (learningObjectId.equals("NONE")) {
			throw new WebApplicationException(Status.NO_CONTENT);
		}
		
		LearningContent providedContent = contentRepository.findOne(learningObjectId);
		if(providedContent == null)
			throw new WebApplicationException(Status.BAD_REQUEST);
		
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int helpNeededContentCount = 0;

		map.put(Constants.FEEDBACK_LIKED, findCountForLikedObject(providedContent, EnumLike.LIKED));
		map.put(Constants.FEEDBACK_DISLIKED, findCountForLikedObject(providedContent, EnumLike.DISLIKED));
		
		List<UserFeedback> helpNeededList = feedbackRepository.findByLearningObjectAndHelpNeeded(providedContent, true);
		if (helpNeededList != null) {
			helpNeededContentCount = helpNeededList.size();
			map.put(Constants.FEEDBACK_HELPNEEDED, helpNeededContentCount);
		}
		
		map.put(Constants.LEARNING_OBJECT_WATCHCOUNT, findCountForContentWatched(providedContent.getUuid()));
		
		return map;
	}
	
	
	/**
	 * Finds a user feedback object for a given user and a given learning object.
	 * 
	 * @param user
	 * @param userId
	 * @param learningObjectId
	 * @param learningObjectType
	 * @param page
	 * @param count
	 * @return
	 */
	@GET
	@Timed
	@Path("/findByUserAndLearningObject")
	public UserFeedback findByUserAndLearningObject(@Auth(required=false) User user,
			@QueryParam("userId") @DefaultValue("NONE")	String userId,
			@QueryParam("learningObjectId") @DefaultValue("NONE") String learningObjectId,
			@QueryParam("learningObjectType") @DefaultValue(Constants.LO_TYPE_CONTENT) String learningObjectType)	//	Default taken as content. 
		{
 		
		if (userId.equals("NONE") || learningObjectId.equals("NONE")) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		
		User providedUser = userRepository.findOne(userId);
		if(providedUser == null) {
			LOG.debug("Invalid user UUID provided.");
			throw new WebApplicationException(Status.BAD_REQUEST);
		} 
		
		//	Handling content of type LearningContent.
		Collection learningObject = findLearningObjectForIdAndType(learningObjectId, learningObjectType);
		if(learningObject == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		
		List<UserFeedback> userFeedbackList = feedbackRepository.findByLearningObjectAndUser(learningObject, providedUser);
				
		if(userFeedbackList != null && userFeedbackList.size() > 0)
			return userFeedbackList.get(0);
		else throw new WebApplicationException(Status.NO_CONTENT);
	}
	
	/**
	 * Finds count of user feedback object for a given Liked flag and a given user feedback object.
	 * 
	 * @param providedContent
	 * @param likedObject
	 * @return
	 */
	private int findCountForLikedObject(Collection providedContent, EnumLike likedObject) {
		// TODO Auto-generated method stub
		List<UserFeedback> likedList = feedbackRepository.findByLearningObjectAndLiked(providedContent, likedObject);
		if (likedList != null) {
			return likedList.size(); 
		} else return 0; 
	}
	
	private int findCountForContentWatched(String providedContentUUID) {
		//	Considering the content to be learning content.
		//	TODO	Need to support this for other collections also. 
		LearningContent content = contentRepository.findOne(providedContentUUID);
		return content.getWatchCount();
	}
	
	/**
	 * Method to find user feedback object on the basis of learning object UUID and type.
	 * 
	 * @param learningObjectId
	 * @param learningObjectType
	 * @return
	 */
	private Collection findLearningObjectForIdAndType(String learningObjectId, String learningObjectType) {
		// TODO Auto-generated method stub
		if(learningObjectType.equalsIgnoreCase(Constants.LO_TYPE_CONTENT)) {
			LearningContent content = contentRepository.findOne(learningObjectId);
			return content;
		} else if(learningObjectType.equalsIgnoreCase(Constants.LO_TYPE_TOPIC)) {
			LearningTopic topic = topicRepository.findOne(learningObjectId);
			return topic;
			//	TODO	-	support for other collections needed here.
		} else LOG.debug("Content type not supported...");
		return null;
	}	
}
