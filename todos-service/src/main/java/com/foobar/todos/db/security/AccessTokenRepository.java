/**
 * 
 */
package com.foobar.todos.db.security;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.pragyasystems.knowledgeHub.api.security.AccessToken;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The spring-data repository for AccessToken
 */
public interface AccessTokenRepository extends
		PagingAndSortingRepository<AccessToken, String> {

}
