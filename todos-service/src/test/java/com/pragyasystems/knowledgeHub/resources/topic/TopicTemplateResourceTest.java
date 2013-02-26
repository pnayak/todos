package com.pragyasystems.knowledgeHub.resources.topic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.google.common.collect.Lists;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.topic.LearningLevel;
import com.pragyasystems.knowledgeHub.api.topic.TopicTemplate;
import com.pragyasystems.knowledgeHub.db.topic.LearningLevelRepository;
import com.pragyasystems.knowledgeHub.db.topic.TopicTemplateRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResourceTest;

@Test(groups = { "checkintest" })
public class TopicTemplateResourceTest extends BaseResourceTest<TopicTemplate> {
	
	private LearningLevelRepository mockLevelRepository;
	private User mockUser;

	@Override
	@BeforeClass
	public void init() {
		
		TopicTemplateRepository repository = mock(TopicTemplateRepository.class);
		this.mockRepository = repository;
		ElasticSearchIndex<TopicTemplate> index = mock(ElasticSearchIndex.class);
		this.mockIndex = index;
		LearningLevelRepository mockLevelRepository = mock(LearningLevelRepository.class);
		this.mockLevelRepository = mockLevelRepository;
		this.resource = new TopicTemplateResource(repository, index, mockLevelRepository);
		
		// The entity that the base test uses for testing
		entity = new TopicTemplate();
		
		// Mock User
		mockUser = mock(User.class);
	}
	
	@Test
	public void testBeforeAdd() {
		
		LearningLevel learn = new LearningLevel("Fake_ID_1");
		LearningLevel apply = new LearningLevel("Fake_ID_2");
		LearningLevel nullLevel = new LearningLevel("NULL_ID");
		
		List<LearningLevel> levelList = Lists.newArrayList();
		levelList.add(learn);
		levelList.add(apply);
		levelList.add(nullLevel);
		
		TopicTemplate providedTopicTemplate = new TopicTemplate("511846484800016990f86233");
		providedTopicTemplate.setLevelList(levelList);
		
		when(mockLevelRepository.findOne("Fake_ID_1")).thenReturn(learn);
		when(mockLevelRepository.findOne("Fake_ID_2")).thenReturn(learn);
		when(mockLevelRepository.findOne("NULL_ID")).thenReturn(null);
		
		TopicTemplateResource templateResource = (TopicTemplateResource)this.resource;
		templateResource.beforeAdd(mockUser, null, providedTopicTemplate);
		
		assertEquals(providedTopicTemplate.getLevelList().size(), 2);
	}
}
