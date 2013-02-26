/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.foobar.todos.KnowledgeServiceConfiguration;
import com.pragyasystems.knowledgeHub.resources.content.ContentOps;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         This class provides any resource ops beans
 */
@Configuration
public class ResourceConfigModule {

	@Autowired
	protected KnowledgeServiceConfiguration configuration;

	@Bean
	protected ContentOps getContentOps() {
		return new ContentOps();
	}
}
