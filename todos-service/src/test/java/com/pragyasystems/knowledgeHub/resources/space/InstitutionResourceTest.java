package com.pragyasystems.knowledgeHub.resources.space;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.foobar.todos.api.Entity;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.google.common.collect.Lists;
import com.pragyasystems.knowledgeHub.api.space.Institution;
import com.pragyasystems.knowledgeHub.db.space.InstitutionRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResourceTest;
import com.yammer.dropwizard.jersey.params.IntParam;

@Test(groups = { "checkintest" })
public class InstitutionResourceTest extends BaseResourceTest<Institution> {

	private LearningSpaceUtil mockLearningSpaceUtil;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pragyasystems.knowledgeHub.resources.BaseResourceTest#init()
	 */
	@Override
	@BeforeClass
	public void init() {

		InstitutionRepository mockInstitutionRepository = mock(InstitutionRepository.class);
		this.mockRepository = mockInstitutionRepository;
		
		this.mockLearningSpaceUtil = mock(LearningSpaceUtil.class);

		ElasticSearchIndex<Institution> mockInstitutionRepositoryIndex = mock(ElasticSearchIndex.class);
		this.mockIndex = mockInstitutionRepositoryIndex;

		this.resource = new InstitutionResource(mockInstitutionRepository,
				mockInstitutionRepositoryIndex, mockLearningSpaceUtil);

		// The entity that the base test uses for testing
		entity = new Institution();
		entity.setTitle("Test Institution Title");
		entity.setAddress("Test address 01231");
	}
	
	/* (non-Javadoc)
	 * @see com.pragyasystems.knowledgeHub.resources.BaseResourceTest#findAllEntities()
	 */
	@Override
	public void findAllEntities() {
		
		@SuppressWarnings("unchecked")
		final Institution entity = new Institution();
		entity.setUuid("511846484800016990f86233");

		List<Institution> entityList = Lists.newArrayList();
		entityList.add(entity);
		Page<Institution> pagedList = new PageImpl<Institution>(entityList);

		when(((InstitutionRepository)mockRepository).findByCategory(any(String.class))).thenReturn(entityList);

		final Iterable<Institution> foundEntityList = resource.findAll(user, null,
				new IntParam("0"), new IntParam("10"));
		Entity foundEntity = foundEntityList.iterator().next();
		assertEquals(foundEntity.getUuid(), "511846484800016990f86233");

		// Negative Testing
//		try {
//			when(((InstitutionRepository)mockRepository).findByCategory(any(String.class))).thenReturn(null);
//			resource.findAll(user, null, new IntParam("0"), new IntParam("10"));
//			fail("Should have thrown a WebApplicationException but did not");
//		} catch (Exception e) {
//			assertTrue(e instanceof WebApplicationException);
//		}
	}
}