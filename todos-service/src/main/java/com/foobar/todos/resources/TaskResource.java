/**
 * 
 */
package com.foobar.todos.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.api.Task;
import com.foobar.todos.db.TaskRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;

/**
 * @author pnayak
 * 
 */
@Component
@Path("/task")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
