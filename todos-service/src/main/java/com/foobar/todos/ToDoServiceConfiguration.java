/**
 * 
 */
package com.foobar.todos;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

/**
 * 
 * @author Prashant Nayak (pnayak)
 * 
 * The configuration class for the ToDo Service.
 * The configuration is read from todo-services.yaml
 * 
 */
public class ToDoServiceConfiguration extends Configuration {

	/**
	 * The id of this service instance.  The intent is that there 
	 * will potentially be multiple deployments of this service
	 * and the instance Id will be used for various purposes, including
	 * configuration, sharding, logging, etc.
	 */
	@NotEmpty
	@JsonProperty
	private String kserviceId;

	public String getKserviceId() {
		return kserviceId;
	}
	
	/**
	 * The hostname of the MongoDB Server
	 */
	@NotEmpty
	@JsonProperty
	private String mongoServer;
	
	public String getMongoServer() {
		return mongoServer;
	}
	
	/**
	 * The hostname of the ElasticSearch Server
	 */
	@NotEmpty
	@JsonProperty
	private String esServer;
	
	public String getEsServer() {
		return esServer;
	}
	
	/**
	 * The interface used by the ElasticSearch
	 * client 
	 */
	@JsonProperty
	private String esClientId;
	
	public String getEsClientId() {
		return esClientId;
	}
	
	/**
	 * The authentication caching policy
	 */
	@NotEmpty
	@JsonProperty
	private String authenticationCachePolicy;

	public String getAuthenticationCachePolicy() {
		return authenticationCachePolicy;
	}
}
