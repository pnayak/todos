package com.pragyasystems.knowledgeHub.resources.topic;

import static org.mockito.Mockito.mock;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.topic.LearningLevel;
import com.pragyasystems.knowledgeHub.db.topic.LearningLevelRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResourceTest;

@Test(groups = { "checkintest" })
public class LearningLevelResourceTest extends BaseResourceTest<LearningLevel> {

	@Override
	@BeforeClass
	public void init() {
		LearningLevelRepository repository = mock(LearningLevelRepository.class);
		this.mockRepository = repository;
		ElasticSearchIndex<LearningLevel> index = mock(ElasticSearchIndex.class);
		this.mockIndex = index;
		this.resource = new LearningLevelResource(repository, index);
		
		// The entity that the base test uses for testing
		entity = new LearningLevel();
	}
}
