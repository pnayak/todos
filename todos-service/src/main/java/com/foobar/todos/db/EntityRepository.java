/**
 * 
 */
package com.foobar.todos.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.pragyasystems.knowledgeHub.api.Collection;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * @author Prashant Nayak (pnayak)
 *
 * The spring-data repository for Learning Content
 */
public interface EntityRepository extends PagingAndSortingRepository<LearningContent, String> {
	
	Page<LearningContent> findAll(Pageable pageable);

    List<LearningContent> findByTitle(String title);

    Page<LearningContent> findByTitle(String Title, Pageable pageable);

    LearningContent findByUuid(String uuid);
        
    List<LearningContent> findByOwnerLearningSpaceUUID(String ownerLearningSpaceUUID, Pageable pageable);
    
    List<LearningContent> findByOwnerLearningSpaceUUIDIn(String[] ids);
    
    List<LearningContent> findByAncestor(Collection parent );

    List<LearningContent> findByAncestorIn(Collection[] parents);

}  