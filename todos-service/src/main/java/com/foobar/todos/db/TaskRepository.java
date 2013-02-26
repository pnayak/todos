package com.foobar.todos.db;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.foobar.todos.api.Task;

/**
 * Spring Data Repository for Tasks
 * 
 * @author pnayak
 * 
 */
public interface TaskRepository extends
		PagingAndSortingRepository<Task, String> {

}
