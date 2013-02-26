package com.pragyasystems.knowledgeHub.resources.space;

import static org.mockito.Mockito.mock;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.space.DeliveryContext;
import com.pragyasystems.knowledgeHub.db.space.DeliveryContextRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResourceTest;

@Test(groups = { "checkintest" })
public class DeliveryContextResourceTest extends
		BaseResourceTest<DeliveryContext> {

	private LearningSpaceUtil mockLearningSpaceUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pragyasystems.knowledgeHub.resources.BaseResourceTest#init()
	 */
	@Override
	@BeforeClass
	public void init() {

		DeliveryContextRepository mockDeliveryContextRepository = mock(DeliveryContextRepository.class);
		this.mockRepository = mockDeliveryContextRepository;
		
		this.mockLearningSpaceUtil = mock(LearningSpaceUtil.class);

		ElasticSearchIndex<DeliveryContext> mockDeliveryContextRepositoryIndex = mock(ElasticSearchIndex.class);
		this.mockIndex = mockDeliveryContextRepositoryIndex;
		
		UserRepository mockUserRepository = mock(UserRepository.class);

		this.resource = new DeliveryContextResource(mockDeliveryContextRepository,
				mockDeliveryContextRepositoryIndex, mockLearningSpaceUtil, mockUserRepository);

		// The entity that the base test uses for testing
		entity = new DeliveryContext();
		entity.setTitle("Test DeliveryContext Title");
	}
}
