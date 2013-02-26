package com.foobar.todos.db;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testng.annotations.Test;

import com.foobar.todos.ToDoServiceConfiguration;
import com.foobar.todos.db.MongoDbConfigModule;
import com.mongodb.Mongo;

/**
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         A test of the MongoDB configuration
 *         Note that these tests are supposed to be run WITHOUT
 *         a MongoDb server running. So, they can be run on the
 *         CI server as well as independently of any specific
 *         configuration on a developer's machine
 */
@Test(groups = { "checkintest" })
public class MongoDbConfigModuleTest {

	@Test
	public void testMongo() {
		ToDoServiceConfiguration mockConfig = mock(ToDoServiceConfiguration.class);
		when(mockConfig.getMongoServer()).thenReturn("localhost");
		MongoDbConfigModule dbConfigModule = new MongoDbConfigModule();
		dbConfigModule.configuration = mockConfig;

		Mongo mongo = dbConfigModule.mongo();
		assertNotNull(mongo);
		assertEquals(mongo.getMongoOptions().connectionsPerHost, 8);
	}

	@Test
	public void testMongoDbFactory() {
		ToDoServiceConfiguration mockConfig = mock(ToDoServiceConfiguration.class);
		when(mockConfig.getMongoServer()).thenReturn("localhost");
		MongoDbConfigModule dbConfigModule = new MongoDbConfigModule();
		dbConfigModule.configuration = mockConfig;
		
		try {
			MongoDbFactory factory = dbConfigModule.mongoDbFactory();
			assertNotNull(factory);
		} catch (Exception e) {
		    fail("Exception trying to get a MongoDbFactory from MongoDbConfigModule", e);
		}
	}

	@Test
	public void testMongoTemplate() {
		ToDoServiceConfiguration mockConfig = mock(ToDoServiceConfiguration.class);
		when(mockConfig.getMongoServer()).thenReturn("localhost");
		MongoDbConfigModule dbConfigModule = new MongoDbConfigModule();
		dbConfigModule.configuration = mockConfig;

		try {
			MongoTemplate mongoTemplate = dbConfigModule.mongoTemplate();
			assertNotNull(mongoTemplate);
		} catch (Exception e) {
			fail("Exception trying to get a MongoTemplate from MongoDbConfigModule", e);
		}
	}
}
