/**
 * 
 */
package com.foobar.todos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.pragyasystems.knowledgeHub.db.group.GroupRepository;
import com.pragyasystems.knowledgeHub.resources.space.LearningSpaceUtil;
import com.pragyasystems.knowledgeHub.security.AuthUtil;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Provide any Spring @Bean configuration as needed
 */
@Configuration
public class KnowledgeServiceConfigModule {
	
	@Autowired
	private GroupRepository groupRepository;

	@Bean
	protected SetupService getSetupService(){
		return new SetupService();
	}
	
	@Bean
	protected LearningSpaceUtil getLearningSpaceUtil(){
		return new LearningSpaceUtil();
	}
	
	@Bean
	protected AuthUtil getAuthorizationUtil(){
		return new AuthUtil(groupRepository);
	}
	
}
