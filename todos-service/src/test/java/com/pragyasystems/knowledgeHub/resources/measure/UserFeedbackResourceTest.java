package com.pragyasystems.knowledgeHub.resources.measure;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.HashMap;
import java.util.List;

import javax.swing.text.AbstractDocument.Content;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.testng.annotations.Test;
import org.testng.collections.Lists;

import com.foobar.todos.constants.Constants;
import com.foobar.todos.db.EntityRepository;
import com.foobar.todos.db.security.AccessTokenRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.Collection;
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.pragyasystems.knowledgeHub.api.measure.UserFeedback;
import com.pragyasystems.knowledgeHub.api.measure.UserFeedback.EnumLike;
import com.pragyasystems.knowledgeHub.api.security.Role;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.topic.LearningTopic;
import com.pragyasystems.knowledgeHub.api.topic.TopicTemplate;
import com.pragyasystems.knowledgeHub.db.group.GroupRepository;
import com.pragyasystems.knowledgeHub.db.measure.UserFeedbackRepository;
import com.pragyasystems.knowledgeHub.db.topic.TopicRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.measure.UserFeedbackResource;
import com.pragyasystems.knowledgeHub.resources.space.LearningSpaceUtil;

/**
 * 
 * @author abhishek.gupta
 *
 */

@Test(groups = { "checkintest" })
public class UserFeedbackResourceTest {
	
	private final UserRepository mockUserRepository = mock(UserRepository.class);
	private final EntityRepository mockContentRepository = mock(EntityRepository.class);
	private final TopicRepository mockTopicRepository = mock(TopicRepository.class);
	private final GroupRepository mockGroupRepository = mock(GroupRepository.class);
	private final UserFeedbackRepository mockUserFeedbackRepository = mock(UserFeedbackRepository.class);
	private final AccessTokenRepository mockTokenRepository = mock(AccessTokenRepository.class);
	
	@SuppressWarnings("rawtypes")
	private final ElasticSearchIndex<UserFeedback> mockFeedbackIndex = mock(ElasticSearchIndex.class);
	private final LearningSpaceUtil mockLearningSpaceUtil = mock(LearningSpaceUtil.class);	
	
	private final UserFeedbackResource resource = new UserFeedbackResource(mockUserFeedbackRepository,
			mockFeedbackIndex,mockUserRepository, mockContentRepository, mockTopicRepository);
	
	@Test
	public void testBeforeAdd() {
		
		final User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setUsername("jsmith");
		user.setPassword("foobar");
		user.setFirstName("Joe");
		user.setLastName("Smith");
		user.setMiddleName("W");
		user.setCity("Delhi");
		user.setCountry("India");
		user.setDescription("Demo User");
		user.seteMail("user@techizen.com");
		user.setLanguage("Hindi");
		user.setPicture("random binary data");
		user.setTimeZone("GMT+5:30");

		final Role role = new Role();
		role.setUuid("511846484800016990f86233");
		role.setRole(Constants.ROLE_STUDENT);
		role.setRoleDescription("A-FAKE-ROLE-DESCRIPTION");
		
		user.addRole(role);
		
		LearningContent content = new LearningContent();
		content.setUuid("511846484800016990f86233");
		content.setVersion("1.0");
		content.setTitle("Test");
		content.setDescription("This is a description");
		
		UserFeedback userFeedback = new UserFeedback();
		userFeedback.setUser(user);
		userFeedback.setLearningObject(content);
		userFeedback.setLiked(EnumLike.LIKED);
		userFeedback.setHelpNeeded(true);
		
		when(mockUserRepository.findOne(any(String.class))).thenReturn(user);
		
		when(mockContentRepository.findOne(any(String.class))).thenReturn(content);
		
		resource.beforeAdd(user, "A-FAKE-PARENT-UUID", userFeedback);
		
		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			resource.beforeAdd(user, "A-FAKE-PARENT-UUID", null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
		
		try {
			userFeedback.getUser().setUuid(null);
			resource.beforeAdd(user, "A-FAKE-PARENT-UUID", userFeedback);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
		
		try {
			userFeedback.setUser(null);
			resource.beforeAdd(user, "A-FAKE-PARENT-UUID", userFeedback);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}		
		
		try {
			userFeedback.getLearningObject().setUuid(null);
			resource.beforeAdd(user, "A-FAKE-PARENT-UUID", userFeedback);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			userFeedback.setLearningObject(null);
			resource.beforeAdd(user, "A-FAKE-PARENT-UUID", userFeedback);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}		
	}
	
	@Test
	public void testFindContentFeedbackForInstructor() {
		final User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setUsername("jsmith");
		user.setPassword("foobar");
		user.setFirstName("Joe");
		user.setLastName("Smith");
		user.setMiddleName("W");
		user.setCity("Delhi");
		user.setCountry("India");
		user.setDescription("Demo User");
		user.seteMail("user@techizen.com");
		user.setLanguage("Hindi");
		user.setPicture("random binary data");
		user.setTimeZone("GMT+5:30");

		final Role role = new Role();
		role.setUuid("511846484800016990f86233");
		role.setRole(Constants.ROLE_INSTRUCTOR);
		role.setRoleDescription("A-FAKE-ROLE-DESCRIPTION");

		user.addRole(role);
		
		LearningContent content = new LearningContent();
		content.setUuid("511846484800016990f86233");
		content.setVersion("1.0");
		content.setTitle("Test");
		content.setDescription("This is a description");
		
		UserFeedback userFeedback = new UserFeedback();
		userFeedback.setUser(user);
		userFeedback.setLearningObject(content);
		userFeedback.setLiked(EnumLike.LIKED);
		userFeedback.setHelpNeeded(true);
		
		when(mockUserRepository.findOne(any(String.class))).thenReturn(user);
		
		when(mockContentRepository.findOne(any(String.class))).thenReturn(content);
		
		//when(mockUserFeedbackRepository.findOne(any(String.class))).thenReturn(userFeedback);
		
		List<UserFeedback> likedList = Lists.newArrayList();
		likedList.add(userFeedback);
		
		when(mockUserFeedbackRepository.findByLearningObjectAndLiked(content, EnumLike.LIKED)).thenReturn(likedList);
		
		when(mockUserFeedbackRepository.findByLearningObjectAndLiked(content, EnumLike.DISLIKED)).thenReturn(likedList);
		
		List<UserFeedback> helpNeededList = Lists.newArrayList(); 
		helpNeededList.add(userFeedback);
		when(mockUserFeedbackRepository.findByLearningObjectAndHelpNeeded(content, true)).thenReturn(helpNeededList);
		
		HashMap<String, Integer> map = resource.findContentFeedbackForInstructor("511846484800016990f86233");
		HashMap<String, Integer> localMap = new HashMap<String, Integer>();

		localMap.put(Constants.FEEDBACK_LIKED, 1);
		localMap.put(Constants.FEEDBACK_DISLIKED, 1);
		localMap.put(Constants.FEEDBACK_HELPNEEDED, 1);
		localMap.put(Constants.LEARNING_OBJECT_WATCHCOUNT, 0);
		assertEquals(map, localMap);
	}
	
	@Test
	private void testFindByUserAndLearningObject() {
		final User user = new User();
		user.setUuid("511846484800016990f86233");
		user.setUsername("jsmith");
		user.setPassword("foobar");
		user.setFirstName("Joe");
		user.setLastName("Smith");
		user.setMiddleName("W");
		user.setCity("Delhi");
		user.setCountry("India");
		user.setDescription("Demo User");
		user.seteMail("user@techizen.com");
		user.setLanguage("Hindi");
		user.setPicture("random binary data");
		user.setTimeZone("GMT+5:30");

		final Role role = new Role();
		role.setUuid("511846484800016990f86233");
		role.setRole("Student");
		role.setRoleDescription("A-FAKE-ROLE-DESCRIPTION");
		
		LearningContent content = new LearningContent();
		content.setUuid("511846484800016990f86233");
		content.setVersion("1.0");
		content.setTitle("Test");
		content.setDescription("This is a description");
		
		final LearningTopic<LearningContent> topic = new LearningTopic<LearningContent>();
		topic.setUuid("511846484800016990f86233");
		topic.setVersion("1.3");
		topic.setTitle("test");
		topic.setDescription("test description");

		List<Collection> children = Lists.newArrayList();
		children.add(content);
		topic.setChildren(children);

		TopicTemplate template = new TopicTemplate();
		template.setUuid("511846484800016990f86233");
		template.setName("Not-Blooms");
		topic.setTemplate(template);		
		
		UserFeedback userFeedbackForContent = new UserFeedback();
		userFeedbackForContent.setUuid("511846484800016990f86233");
		userFeedbackForContent.setUser(user);
		userFeedbackForContent.setLearningObject(content);
		userFeedbackForContent.setLiked(EnumLike.LIKED);
		userFeedbackForContent.setHelpNeeded(true);
		
		UserFeedback userFeedbackForTopic = new UserFeedback();
		userFeedbackForTopic.setUuid("511846484800016990f86233");
		userFeedbackForTopic.setUser(user);
		userFeedbackForTopic.setLearningObject(topic);
		userFeedbackForTopic.setLiked(EnumLike.LIKED);
		userFeedbackForTopic.setHelpNeeded(true);
		
		List<UserFeedback> userFeedbackList = Lists.newArrayList();
		
		when(mockUserRepository.findOne(any(String.class))).thenReturn(user);
		
		when(mockContentRepository.findOne(any(String.class))).thenReturn(content);
		
		when(mockTopicRepository.findOne(any(String.class))).thenReturn(topic);
		
		userFeedbackList.add(userFeedbackForContent);
		when(mockUserFeedbackRepository.findByLearningObjectAndUser(content, user)).thenReturn(userFeedbackList);
		
		//	Checking for learning object as learning content.
		UserFeedback userFeedback2 = resource.findByUserAndLearningObject(user, "511846484800016990f86233", 
				"511846484800016990f86233", Constants.LO_TYPE_CONTENT);
		assertEquals(userFeedback2, userFeedbackForContent);

		userFeedbackList.clear();
		
		userFeedbackList.add(userFeedbackForTopic);
		when(mockUserFeedbackRepository.findByLearningObjectAndUser(topic, user)).thenReturn(userFeedbackList);		
		//	Checking for learning object as learning content.
		userFeedback2 = resource.findByUserAndLearningObject(user, "511846484800016990f86233", 
				"511846484800016990f86233", Constants.LO_TYPE_TOPIC);
		assertEquals(userFeedback2, userFeedbackForTopic);
	}
}
