package com.foobar.todos;

import com.github.pnayak.dropwizard.spring.AutoWiredService;
import com.yammer.dropwizard.config.Environment;

/**
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The ToDo's Service.
 * 
 *         This class serves as the main point of initialization and
 *         configuration for all underlying service, resource, health-checks,
 *         etc.
 * 
 */
public class ToDoService extends
		AutoWiredService<ToDoServiceConfiguration> {

	public static void main(String[] args) throws Exception {
		new ToDoService().run(args);
	}

	private ToDoService() {
		super("ToDo Service", "com.foobar.todos");
	}
}
