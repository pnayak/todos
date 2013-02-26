package com.foobar.todos.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.foobar.todos.api.Entity;
import com.foobar.todos.api.security.User;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.google.common.collect.Lists;
import com.yammer.dropwizard.jersey.params.IntParam;

@Test(groups = { "checkintest" })
public abstract class BaseResourceTest<T extends Entity> {

	protected PagingAndSortingRepository<T, String> mockRepository;
	protected ElasticSearchIndex<T> mockIndex;
	protected BaseResource<T> resource;
	protected T entity;
	protected User user = new User();

	/**
	 * Tests need to implement this and initialize the mockRepository, mockIndex
	 * and resource under test in this method. Also initialize the entity
	 * concrete sub-type
	 */
	@BeforeClass
	public abstract void init();

	@Test
	public void addNewEntity() {

		entity.setUuid("511846484800016990f86233");

		when(mockRepository.save(entity)).thenReturn(entity);

		final Response response = resource.add(user, entity);

		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");

		assertEquals(response.getStatus(), 201);

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = resource.add(user, null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void updateEntity() {

		entity.setUuid("511846484800016990f86233");

		when(mockRepository.findOne("511846484800016990f86233")).thenReturn(
				entity);
		when(mockRepository.save(entity)).thenReturn(entity);

		final Response response = resource.update(user, null, entity);

		assertEquals(response.getMetadata().get("Location").get(0).toString(),
				"/511846484800016990f86233");

		assertEquals(response.getStatus(), 201);

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = resource.update(user, null, null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			negResponse = resource.update(user, null, entity);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			when(mockRepository.findOne("511846484800016990f86233"))
					.thenReturn(null);
			negResponse = resource.update(user, null, entity);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void findSingleEntityByUuid() {

		@SuppressWarnings("unchecked")
		final T entity = (T) new Entity();
		entity.setUuid("511846484800016990f86233");

		when(mockRepository.findOne("511846484800016990f86233")).thenReturn(
				entity);

		final T foundEntity = resource
				.findOne(user, "511846484800016990f86233");
		assertEquals(foundEntity.getUuid(), "511846484800016990f86233");

		// Negative Testing
		try {
			resource.findOne(user, null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			when(mockRepository.findOne("511846484800016990f86233"))
					.thenReturn(null);
			resource.findOne(user, "Topic_AAABBB123");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void findAllEntities() {

		@SuppressWarnings("unchecked")
		final T entity = (T) new Entity();
		entity.setUuid("511846484800016990f86233");

		List<T> entityList = Lists.newArrayList();
		entityList.add(entity);
		Page<T> pagedList = new PageImpl<T>(entityList);

		when(mockRepository.findAll(any(Pageable.class))).thenReturn(pagedList);

		final Iterable<T> foundEntityList = resource.findAll(user,
				new IntParam("0"), new IntParam("10"));
		Entity foundEntity = foundEntityList.iterator().next();
		assertEquals(foundEntity.getUuid(), "511846484800016990f86233");

		// Negative Testing
		try {
			when(mockRepository.findAll(any(Pageable.class))).thenReturn(null);
			resource.findAll(user, new IntParam("0"), new IntParam("10"));
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}

	@Test
	public void deleteEntity() {

		when(mockRepository.findOne("511846484800016990f86233")).thenReturn(
				entity);

		final Response response = resource.delete(user,
				"511846484800016990f86233");

		assertEquals(response.getStatus(), 204);

		// Negative testing
		@SuppressWarnings("unused")
		Response negResponse;
		try {
			negResponse = resource.delete(user, null);
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}

		try {
			when(mockRepository.findOne("511846484800016990f86233"))
					.thenReturn(null);
			negResponse = resource.delete(user, "511846484800016990f86233");
			fail("Should have thrown a WebApplicationException but did not");
		} catch (Exception e) {
			assertTrue(e instanceof WebApplicationException);
		}
	}
}
