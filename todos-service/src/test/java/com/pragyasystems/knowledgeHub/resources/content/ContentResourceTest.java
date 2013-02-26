package com.pragyasystems.knowledgeHub.resources.content;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import com.pragyasystems.knowledgeHub.api.content.LearningContent;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.security.AuthUtil;
import com.yammer.dropwizard.jersey.params.IntParam;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         Test the ContentResource, specifically its methods to add, update,
 *         find, delete, etc.
 */
@Test(groups = { "checkintest" })
public class ContentResourceTest {

	private final EntityRepository mockContentRepository = mock(EntityRepository.class);
	@SuppressWarnings("unchecked")
	private final ElasticSearchIndex<LearningContent> contentIndex = mock(ElasticSearchIndex.class);
	private final UserRepository mockUserRepository = mock(UserRepository.class);
	private final SpaceRepository mockSpaceRepository = mock(SpaceRepository.class);

	private final ContentResource resource = new ContentResource(
			mockContentRepository, contentIndex, mockUserRepository,
			mockSpaceRepository);
	private final AuthUtil mockAuthutil = mock(AuthUtil.class);

	@Test
	public void addNewContent() {
		resource.setAuthUtil(mockAuthutil);
		LearningContent content = new LearningContent();
		content.setUuid("511846484800016990f86233");
		content.setVersion("1.0");
		content.setTitle("Test");
		content.setDescription("This is a description");

		User owner = new User("511846484800016990f86233");

		content.setOwner(owner);

		LearningSpace parentSpace = new LearningSpace();
		parentSpace.setUuid("1234");

		when(mockContentRepository.save(content)).thenReturn(content);
		when(mockUserRepository.findOne(anyString())).thenReturn(owner);
		when(mockSpaceRepository.findOne(anyString())).thenReturn(parentSpace);

		final Response response = resource.add(owner,"1234", content);

		assertEquals(response.getStatus(), 201);
		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");
		verify(mockAuthutil, times(1)).assertCanWrite(any(User.class), any(LearningSpace.class));

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = resource.add(owner,"123", null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		// invalid parent learning space provided
		try {
			when(mockUserRepository.findOne(anyString())).thenReturn(owner);
			when(mockSpaceRepository.findOne(anyString())).thenReturn(
					null);
			negResponse = resource.add(owner,"123", content);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void findAllContent() {
		User user = new User();
		LearningContent content = new LearningContent();
		content.setUuid("511846484800016990f86233");
		content.setVersion("1.0");
		content.setTitle("Test");
		content.setDescription("This is a description");
		
		final LearningSpace space = new LearningSpace();
		space.setUuid("511846484800016990f86233");
		space.setVersion("1.3");
		space.setTitle("test");
		space.setDescription("test description");
		
		List<LearningContent> contentList = Lists.newArrayList();
		contentList.add(content);
		when(mockSpaceRepository.findOne(any(String.class))).thenReturn(space);
		when(mockContentRepository.findByAncestor(space)).thenReturn(contentList);
/*		when(
				mockContentRepository.findByAncestor(
						any(LearningSpace.class))).thenReturn(contentList);
*/
		final Iterable<LearningContent> foundContentList = resource.findAll(user,
				"123", new IntParam("1"), new IntParam("10"));

		LearningContent foundContent = foundContentList.iterator().next();
		assertEquals(foundContent.getUuid(), "511846484800016990f86233");

		// Negative Testing
		try {
			when(mockContentRepository.findByAncestor(space)).thenReturn(null);

/*			when(
					mockContentRepository.findByAncestor(
							any(LearningSpace.class))).thenReturn(null);
*/							
			resource.findAll(user,"123", new IntParam("1"), new IntParam("10"));
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}
}
