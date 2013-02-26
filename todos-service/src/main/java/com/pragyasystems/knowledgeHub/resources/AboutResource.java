/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.KnowledgeServiceConfiguration;
import com.foobar.todos.api.About;
import com.google.common.base.Optional;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The Resource that is used to obtain information about the Knowledge
 *         Service
 * 
 *         It is paired up with the {@About} Representation.
 * 
 *         Both of these are @Autowired in the KnowledgeService class
 * 
 */
@Component
@Path("/about")
@Produces(MediaType.APPLICATION_JSON)
public class AboutResource {

	private String kserviceId;

	/**
	 * @param kserviceId
	 */
	@Autowired
	public AboutResource(KnowledgeServiceConfiguration config) {
		super();
		this.kserviceId = config.getKserviceId();
	}

	@GET
	@Timed
	public About tellAbout(@Auth(required = false) User user) {
		
		String username = null;
		if (user != null) {
			username = user.getUsername();
		}
		
		return new About("Pragya Systems Knowledge Service.  Id = "
				+ kserviceId + " username = " + Optional.fromNullable(username));
	}
	
	@OPTIONS
	@Timed
	public Response aboutOptions(@Auth(required = false) User user) {	
		// Only allow access from our own domain (can be changed to allow other domains if needed)
		return Response.ok().header("Access-Control-Allow-Origin", "http://staging.pragyasystems.com").build();
	}

}
