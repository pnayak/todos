/**
 * 
 */
package com.foobar.todos.db;

import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.foobar.todos.KnowledgeServiceConfiguration;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         This class represents the required beans to connect to MongoDB
 */
@Configuration
@EnableMongoRepositories
public class MongoDbConfigModule extends AbstractMongoConfiguration {

	private static final Logger LOG =  LoggerFactory
			.getLogger(MongoDbConfigModule.class);

	private String DATABASE_NAME = "knowledgeHub";

	@Autowired
	protected KnowledgeServiceConfiguration configuration;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.mongodb.config.AbstractMongoConfiguration#mongo
	 * ()
	 */
	@Override
	public Mongo mongo() {

		// Mongo options TODO - we need to get sane options in place
		MongoOptions mongoOptions = new MongoOptions();
		mongoOptions.connectionsPerHost = 8;
		// mongoOptions.threadsAllowedToBlockForConnectionMultiplier = 4;
		// mongoOptions.connectTimeout = 1000;
		// mongoOptions.maxWaitTime = 1500;
		// mongoOptions.autoConnectRetry = Boolean.TRUE;
		// mongoOptions.socketKeepAlive = Boolean.TRUE;
		// mongoOptions.socketTimeout = 1500;
		// mongoOptions.slaveOk = true;
		// mongoOptions.wtimeout = 0;

		// TODO: Seriously consider switching to MongoFactoryBean, especially
		// once
		// we start to use @Repository consistently
		// http://static.springsource.org/spring-data/data-mongodb/docs/current/reference/html/#mongo.mongo-java-config
		Mongo mongo = null;

		String mongoServer = null;

		if (configuration != null) {
			mongoServer = configuration.getMongoServer();
			LOG.info("Mongo Server configured to: " + mongoServer);
		} else {
			LOG.warn(" ******* Unable to get Mongo Server configuration... defaulting to localhost");
		}
		
		try {
			mongo = new Mongo(mongoServer, mongoOptions);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mongo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mongodb.config.AbstractMongoConfiguration#
	 * mongoDbFactory()
	 */
	@Override
	public SimpleMongoDbFactory mongoDbFactory() throws Exception {
		// UserCredentials userCredentials = new UserCredentials("joe",
		// "secret");
		// return new SimpleMongoDbFactory(new Mongo(), "database",
		// userCredentials);
		SimpleMongoDbFactory factory = new SimpleMongoDbFactory(mongo(),
				getDatabaseName());
		return factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mongodb.config.AbstractMongoConfiguration#
	 * mongoTemplate()
	 */
	@Override
	public MongoTemplate mongoTemplate() throws Exception {
		return super.mongoTemplate();
		// return new MongoTemplate(mongoDbFactory());
	}

	@Bean
	public GridFsTemplate gridFSTemplate() throws Exception {
		return new GridFsTemplate(this.mongoDbFactory(),
				super.mappingMongoConverter());
		// return new MongoTemplate(mongoDbFactory());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mongodb.config.AbstractMongoConfiguration#
	 * getMappingBasePackage()
	 */
	@Override
	public String getMappingBasePackage() {
		return "com.pragyasystems.knowledgeHub.db";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.mongodb.config.AbstractMongoConfiguration#
	 * getDatabaseName()
	 */
	@Override
	protected String getDatabaseName() {
		return DATABASE_NAME;
	}
}
