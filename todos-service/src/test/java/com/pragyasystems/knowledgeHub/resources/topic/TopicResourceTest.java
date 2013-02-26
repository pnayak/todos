package com.pragyasystems.knowledgeHub.resources.topic;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.springframework.data.domain.Pageable;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.foobar.todos.db.EntityRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.Collection;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.api.topic.LearningTopic;
import com.pragyasystems.knowledgeHub.api.topic.TopicTemplate;
import com.pragyasystems.knowledgeHub.db.space.DeliveryContextRepository;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.topic.TopicRepository;
import com.pragyasystems.knowledgeHub.db.topic.TopicTemplateRepository;
import com.yammer.dropwizard.jersey.params.IntParam;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Test the TopicResource, specifically its methods to add, update,
 *         find, delete, etc.
 */

@Test(groups = { "checkintest" })
public class TopicResourceTest {

	private final TopicRepository mockTopicRepository = mock(TopicRepository.class);
	private final ElasticSearchIndex<LearningTopic<LearningContent>> mockTopicIndex = mock(ElasticSearchIndex.class);
	private final TopicTemplateRepository mockTemplateRepository = mock(TopicTemplateRepository.class);
	private EntityRepository mockContentRepository = mock(EntityRepository.class);
	private SpaceRepository mockSpaceRepository = mock(SpaceRepository.class);
	private DeliveryContextRepository mockdeliveryContextRepository= mock(DeliveryContextRepository.class);;
	private final TopicResource resource = new TopicResource(
			mockTopicRepository, mockTopicIndex, mockTemplateRepository,
			mockContentRepository, mockSpaceRepository,mockdeliveryContextRepository);
	
	private final User mockUser = mock(User.class);

	@SuppressWarnings("unchecked")
	@Test
	public void addNewTopic() {

		final LearningTopic<LearningContent> topic = new LearningTopic<LearningContent>();
		topic.setUuid("511846484800016990f86233");
		topic.setVersion("1.3");
		topic.setTitle("test");
		topic.setDescription("test description");

		final LearningContent content = new LearningContent();
		content.setUuid("Content_AAABBB123");
		content.setVersion("1.4");
		content.setTitle("test");
		content.setDescription("test description");
		List<Collection> children = Lists.newArrayList();
		children.add(content);
		topic.setChildren(children);

		TopicTemplate template = new TopicTemplate();
		template.setUuid("template-1212121212");
		template.setName("Not-Blooms");
		topic.setTemplate(template);

		LearningSpace space = new LearningSpace();
		space.setUuid("space-123123231");

		when(mockTopicRepository.save(topic)).thenReturn(topic);
		when(mockTemplateRepository.findOne(template.getUuid())).thenReturn(
				template);
		when(mockContentRepository.findOne(anyString())).thenReturn(content);
		when(mockSpaceRepository.findOne(anyString())).thenReturn(space);

		final Response response = resource.add(mockUser, new String("space-123123231"),
				topic);
		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");

		assertEquals(response.getStatus(), 201);

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = resource.add(mockUser, new String("123"), null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// Null parentSpaceId
		try {
			negResponse = resource.add(mockUser, null, topic);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setOrUpdateTopicTemplate() {

		final LearningTopic<LearningContent> topic = new LearningTopic<LearningContent>();
		topic.setUuid("511846484800016990f86233");
		topic.setVersion("1.3");
		topic.setTitle("test");
		topic.setDescription("test description");

		final LearningContent content = new LearningContent();
		content.setUuid("Content_AAABBB123");
		content.setVersion("1.4");
		content.setTitle("test");
		content.setDescription("test description");
		List<Collection> children = Lists.newArrayList();
		children.add(content);
		topic.setChildren(children);

		TopicTemplate template = new TopicTemplate();
		template.setUuid("template-1212121212");
		template.setName("Not-Blooms");
		topic.setTemplate(template);

		when(mockTopicRepository.findOne(anyString())).thenReturn(topic);
		when(mockTemplateRepository.findOne(anyString())).thenReturn(template);

		final Response response = resource.setTemplate(
				new String("511846484800016990f86233"), new String("template-1212121212"));
		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");

		assertEquals(response.getStatus(), 201);

		assertEquals(topic.getTemplate().getUuid(), "template-1212121212");

		// Negative Testing
		try {
			// Test for non-existent topic
			when(mockTopicRepository.findOne(anyString())).thenReturn(null);
			resource.setTemplate(new String("511846484800016990f86233"), new String(
					"template-1212121212"));
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			// Test for non-existent template
			when(mockTopicRepository.findOne(anyString())).thenReturn(topic);
			when(mockTemplateRepository.findOne(anyString())).thenReturn(null);
			resource.setTemplate(new String("511846484800016990f86233"), new String(
					"template-1212121212"));
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void findSingleTopicByGuid() {

		final LearningTopic<LearningContent> topic = new LearningTopic<LearningContent>();
		topic.setUuid("511846484800016990f86233");
		topic.setVersion("1.3");
		topic.setTitle("test");
		topic.setDescription("test description");

		final LearningContent content = new LearningContent();
		content.setUuid("Content_AAABBB123");
		content.setVersion("1.4");
		content.setTitle("test");
		content.setDescription("test description");
		List<Collection> children = Lists.newArrayList();
		children.add(content);
		topic.setChildren(children);

		when(mockTopicRepository.findOne("511846484800016990f86233")).thenReturn(
				topic);

		final LearningTopic<LearningContent> foundTopic = resource
				.findOne(mockUser, "511846484800016990f86233");
		assertEquals(foundTopic.getUuid(), "511846484800016990f86233");

		// Negative Testing
		try {
			when(mockTopicRepository.findOne("AAABBB")).thenReturn(null);
			resource.findOne(mockUser, "AAABBB");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			when(mockTopicRepository.findOne("511846484800016990f86233")).thenReturn(
					null);
			resource.findOne(mockUser, "511846484800016990f86233");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void findAllTopics() {

		final LearningTopic<LearningContent> topic = new LearningTopic<LearningContent>();
		topic.setUuid("511846484800016990f86233");
		topic.setVersion("1.3");
		topic.setTitle("test");
		topic.setDescription("test description");

		final LearningContent content = new LearningContent();
		content.setUuid("Content_AAABBB123");
		content.setVersion("1.4");
		content.setTitle("test");
		content.setDescription("test description");
		List<Collection> children = Lists.newArrayList();
		children.add(content);
		topic.setChildren(children);

		List<LearningTopic<LearningContent>> topicList = Lists.newArrayList();
		topicList.add(topic);
		
		LearningSpace space = new LearningSpace();

		when(mockTopicRepository.findByAncestor(any(LearningSpace.class),
						any(Pageable.class))).thenReturn(topicList);
		when(mockSpaceRepository.findOne(anyString())).thenReturn(space);

		final Iterable<LearningTopic<LearningContent>> foundtopicList = resource
				.findAll(mockUser, "fake-parent-id", new IntParam("0"), new IntParam("10"));
		LearningTopic<LearningContent> foundContent = foundtopicList.iterator()
				.next();
		assertEquals(foundContent.getUuid(), "511846484800016990f86233");

		// Negative Testing
		try {
			when(mockTopicRepository.findByAncestor(any(LearningSpace.class),
					any(Pageable.class))).thenReturn(null);
			resource.findAll(mockUser, "fake-parent-id", new IntParam("0"), new IntParam("10"));
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void deleteTopic() {

		LearningTopic<LearningContent> topic = new LearningTopic<LearningContent>();
		when(mockTopicRepository.findOne(anyString())).thenReturn(topic);
		final Response response = resource.delete(mockUser, "511846484800016990f86233");

		assertEquals(response.getStatus(), 204);

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			when(mockTopicRepository.findOne(anyString())).thenReturn(null);
			negResponse = resource.delete(mockUser, "511846484800016990f86233");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}
}
