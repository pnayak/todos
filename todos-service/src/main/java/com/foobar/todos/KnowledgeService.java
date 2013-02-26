package com.foobar.todos;

import com.github.pnayak.dropwizard.spring.AutoWiredService;
import com.yammer.dropwizard.config.Environment;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The Pragya Knowledge Service.
 * 
 *         This class serves as the main point of initialization and
 *         configuration for all underlying service, resource, health-checks,
 *         etc.
 * 
 */
public class KnowledgeService extends
		AutoWiredService<KnowledgeServiceConfiguration> {

	public static void main(String[] args) throws Exception {
		new KnowledgeService().run(args);
	}

	private KnowledgeService() {
		super("Pragya Knowledge Service", "com.pragyasystems.knowledgeHub");
	}
}
