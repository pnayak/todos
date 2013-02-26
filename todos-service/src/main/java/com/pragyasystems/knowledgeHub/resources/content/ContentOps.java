/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.db.EntityRepository;
import com.pragyasystems.knowledgeHub.api.Collection;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.topic.LearningTopic;
import com.pragyasystems.knowledgeHub.db.topic.TopicRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 */
@Component
public class ContentOps {
	
	@Autowired private EntityRepository contentRepository;
	@Autowired private TopicRepository topicRepository;
	@Autowired private UserRepository userRepository;

	/**
	 * Will create a copy, save it and return the copy
	 * 
	 * @param original
	 * @param newAncestor
	 * @param newOwner
	 * @return
	 */
	public LearningContent createCopy(String originalUuid,
			String newAncestorUuid, String newOwnerUuid) {

		User newOwner = userRepository.findOne(newOwnerUuid);
		// TODO checks for new owner
		
		LearningContent original = contentRepository.findOne(originalUuid);
		//TODO checks for original
		
		LearningTopic newAncestor = topicRepository.findOne(newAncestorUuid);
		
		LearningContent copy = new LearningContent();

		// Entity
		// no copy of UUID, version
		copy.setLastUpdatedTime(0l);

		// Collection
		copy.setTitle(original.getTitle()); // retain the Title
		copy.setDescription(original.getDescription()); // retain the Description
		copy.setOwner(newOwner); // set owner to new owner
		copy.setAncestor(newAncestor); // set ancestor to target
		copy.setChildren(null); // TODO - do children need to be copied? LC does
								// not have children

		// DeliverableCollection
		copy.setOrderedChildren(null); // TODO - this attribute is unused at
										// this point
		copy.setSharingType(original.getSharingType()); // retain the sharing
														// type
		copy.setOriginChangeAware(original.getOriginChangeAware()); // retain
																	// the
		// originChangeAware
		// setting

		// LearningContent
		copy.setUri(original.getUri()); // Retain the URI
		copy.setOwnerLearningSpaceUUID(newOwner.getPersonalLearningSpaceUUID()); // Set
																					// to
																					// new
																					// Owners's
		copy.setContentId(original.getContentId()); // Content is not copied
		copy.setType(original.getType()); // Retain content type
		copy.setWatchCount(0); // Reset watch count
		copy.setVariants(original.getVariants()); // Retain variants
		
		LearningContent savedCopy = contentRepository.save(copy); 
		
		return savedCopy;
	}

	/**
	 * Will move the content, save it 
	 * 
	 * @param original
	 * @param newAncestor
	 * @param newOwner
	 */
	public LearningContent move(LearningContent original, Collection newAncestor,
			User newOwner) {

		// Entity
		// do not modify of UUID, version
		original.setLastUpdatedTime(System.currentTimeMillis());

		// Collection
		// this.setTitle(getTitle()); // retain the Title
		// this.setDescription(getDescription()); // retain the Description
		original.setOwner(newOwner); // set owner to new owner
		original.setAncestor(newAncestor); // set ancestor to target
		// this.setChildren(null); //TODO - do children need to be have their
		// owner set?

		// DeliverableCollection
		// this.setOrderedChildren(null); //TODO - this attribute is unused at
		// this point
		// this.setSharingType(getSharingType()); // retain the sharing type
		// this.setOriginChangeAware(getOriginChangeAware()); // retain the
		// originChangeAware setting

		// LearningContent
		// this.setUri(getUri()); // Retain the URI
		original.setOwnerLearningSpaceUUID(newOwner
				.getPersonalLearningSpaceUUID()); // Set
													// to
													// new
													// Owners's
		// this.setContentId(getContentId()); // Content is not copied
		// this.setType(getType()); // Retain content type
		// this.setWatchCount(0); // Reset watch count
		// this.setVariants(getVariants()); //Retain variants
		
		LearningContent updatedOriginal = contentRepository.save(original); 
		
		return updatedOriginal;
	}

}
