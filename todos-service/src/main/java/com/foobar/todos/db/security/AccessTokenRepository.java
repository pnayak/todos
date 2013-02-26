/**
 * 
 */
package com.foobar.todos.db.security;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.foobar.todos.api.security.AccessToken;

/**
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The spring-data repository for AccessToken issued after login
 */
public interface AccessTokenRepository extends
		PagingAndSortingRepository<AccessToken, String> {

}
