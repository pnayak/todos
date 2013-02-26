/**
 * 
 */
package com.foobar.todos.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.foobar.todos.api.Task;
import com.foobar.todos.db.TaskRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;

/**
 * @author pnayak
 * 
 */
public class TaskResource extends BaseResource<Task> {

	private static final Logger LOG = LoggerFactory
			.getLogger(TaskResource.class);
	private TaskRepository taskRepository;
	private ElasticSearchIndex<Task> taskIndex;

	/**
	 * @param contentRepository
	 */
	@Autowired
	public TaskResource(TaskRepository taskRepository,
			ElasticSearchIndex<Task> elasticSearchIndexForTask) {
		super(taskRepository, elasticSearchIndexForTask);
		this.taskRepository = taskRepository;
		this.taskIndex = elasticSearchIndexForTask;
	}

}
