package com.foobar.todos.db.security;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.foobar.todos.api.security.User;

/**
 * Repository for User documents
 * 
 * @author pnayak
 * 
 */
public interface UserRepository extends
		PagingAndSortingRepository<User, String> {

	public List<User> findByUsername(String username);

}
