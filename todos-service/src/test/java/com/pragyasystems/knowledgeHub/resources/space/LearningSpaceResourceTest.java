package com.pragyasystems.knowledgeHub.resources.space;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResourceTest;

@Test(groups = { "checkintest" })
public class LearningSpaceResourceTest extends BaseResourceTest<LearningSpace> {

	private LearningSpaceUtil mockLearningSpaceUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pragyasystems.knowledgeHub.resources.BaseResourceTest#init()
	 */
	@Override
	@BeforeClass
	public void init() {

		SpaceRepository mockSpaceRepository = mock(SpaceRepository.class);
		this.mockRepository = mockSpaceRepository;
		
		mockLearningSpaceUtil = mock(LearningSpaceUtil.class);

		ElasticSearchIndex<LearningSpace> mockSpaceIndex = mock(ElasticSearchIndex.class);
		this.mockIndex = mockSpaceIndex;
		this.resource = new LearningSpaceResource(mockSpaceRepository,
				mockSpaceIndex, mockLearningSpaceUtil);

		// The entity that the base test uses for testing
		entity = new LearningSpace();
	}

	@Test
	public void getEnrolledLearningSpaceForUser() {
		LearningSpaceResource resource = (LearningSpaceResource) this.resource;
		resource.getEnrolledLearningSpaceForUser("user-12345612");
		verify(mockLearningSpaceUtil).getEnrolledLearningSpaceForUser(
				"user-12345612");
	}
}