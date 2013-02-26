/**
 * 
 */
package com.foobar.todos.elasticsearch;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.foobar.todos.KnowledgeServiceConfiguration;
import com.foobar.todos.api.Entity;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Elastic Search bindings
 */
@Configuration
public class ElasticSearchConfigModule {

	private static final Logger LOG = LoggerFactory
			.getLogger(ElasticSearchConfigModule.class);

	@Autowired
	protected KnowledgeServiceConfiguration configuration;

	@Bean
	@Scope("singleton")
	protected Client client() {

		String esServer = null;
		String esClientId = null;

		if (configuration != null) {
			esServer = configuration.getEsServer();
			LOG.info("ElasticSearch Server configured to: " + esServer);

			esClientId = configuration.getEsClientId();
			LOG.info("ElasticSearch client IP configured to: " + esClientId);

		} else {
			LOG.warn(" ******* Unable to get ElasticSearch Server configuration...");
		}

		// Use Unicast discovery for now until we figure out how to get
		// Multicast to work on R-space
		Settings settings = null;
		if (esClientId != null) {
			settings = ImmutableSettings.settingsBuilder()
					.put("http.enabled", "false")
					.put("network.host", esClientId)
					.put("transport.tcp.port", "9300-9400")
					.put("discovery.zen.ping.multicast.enabled", "false")
					.put("discovery.zen.ping.unicast.hosts", esServer).build();
		} else {
			settings = ImmutableSettings.settingsBuilder()
					.put("http.enabled", "false")
					.put("transport.tcp.port", "9300-9400")
					.put("discovery.zen.ping.multicast.enabled", "false")
					.put("discovery.zen.ping.unicast.hosts", esServer).build();
		}

		Node node = nodeBuilder().client(true).settings(settings)
				.clusterName("elasticsearch").node();

		Client client = node.client();
		LOG.debug("*** Initialized ElasticSearch client (CLUSTER) " + client);

		return client;
	}

	@Bean
	protected ElasticSearchIndex<Entity> elasticSearchIndexForEntity() {
		ElasticSearchIndex<Entity> esi = new ElasticSearchIndex<Entity>(
				Entity.class, client());
		LOG.debug("*** Initialized ElasticSearch Entity index with CLUSTER client "
				+ esi);
		return esi;
	}

}
