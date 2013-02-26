/**
 * 
 */
package com.foobar.todos.resources;

import java.lang.reflect.InvocationTargetException;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import com.foobar.todos.api.Entity;
import com.foobar.todos.api.security.User;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.foobar.todos.util.CustomBeanUtilBean;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;

/**
 * 
 * @Author Prashant Nayak (pnayak)
 * 
 *         The base resource providing base implementation of all basic CRUD
 *         services. Each method has a before and after method that sub-classes
 *         can override to provide custom behaviour
 * 
 */
@Component
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public abstract class BaseResource<T extends Entity> {

	protected static final Logger LOG = LoggerFactory
			.getLogger(BaseResource.class);
	protected PagingAndSortingRepository<T, String> repository;
	protected ElasticSearchIndex<T> index;

	public BaseResource(PagingAndSortingRepository<T, String> repository,
			ElasticSearchIndex<T> index) {
		super();
		this.repository = repository;
		this.index = index;
	}

	@GET
	@Path("/{uuid}")
	@Timed
	public T findOne(@Auth(required = false) User user, @PathParam("uuid") String uuid) {

		if (uuid == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// logic to be applied before a find is executed
		beforeFindOne(user, uuid);

		T foundLevel = repository.findOne(uuid.toString());

		if (foundLevel != null) {
			return foundLevel;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	/**
	 * Any pre-findOne logic - including AuthZ checks
	 * 
	 * @param user
	 * @param uuid
	 */
	protected void beforeFindOne(User user, String uuid) {
		// TODO - implemented in subclasses
	}

	@GET
	@Timed
	public Iterable<T> findAll(@Auth(required = false) User user,
			@QueryParam("page") @DefaultValue("0") IntParam page,
			@QueryParam("count") @DefaultValue("20") IntParam count) {

		// NOTE: It is left to the sub-classing resource to validate/use
		// the parentSpaceId. It is included here since most of our findAll's
		// SHOULD use a filter by LearningSpace ID... Otherwise the number
		// of results and the authorization checks for the same would get
		// really MESSY

		// logic to be applied before a find is executed
		beforeFindAll(user);

		Pageable pageable = new PageRequest(page.get(), count.get());

		Iterable<T> entityList = repository.findAll(pageable);

		if (entityList != null) {
			return entityList;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	/**
	 * Any pre-findAll logic, including AuthZ checks
	 * 
	 * @param user
	 * @param parentSpaceId
	 */
	private void beforeFindAll(User user) {
		// TODO implemented in subclasses
	}

	@PUT
	@Timed
	public Response add(@Auth(required = false) User user, @Valid T providedEntity) {

		// NOTE: It is left to the sub-classing resource to validate/use
		// the parentSpaceId. It is included here since most of our add's
		// SHOULD use a filter by LearningSpace - otherwise it is not clear
		// where you are trying to create the entity

		if (providedEntity == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// Do any pre-processing before actually saving
		beforeAdd(user, providedEntity);

		// Save the entity
		T addedEntity = repository.save(providedEntity);

		LOG.debug("Added Entity => [" + addedEntity.getUuid() + " : "
				+ addedEntity.getVersion() + "]");

		// Perform any post-processsing after save
		afterAdd(user, addedEntity);

		// Finally add the entity to the index
		index.addToIndex(addedEntity);

		return Response
				.created(
						UriBuilder.fromPath("/{uuid}").build(
								addedEntity.getUuid())).entity(addedEntity)
				.build();
	}

	/**
	 * Implementors of BaseResource should override this method as necessary to
	 * do any pre-processing logic prior to creating the entity, including any
	 * AuthZ checks
	 * 
	 * @param user
	 * @param providedEntity
	 */
	protected void beforeAdd(User user, T providedEntity) {
		// Override in sub-class
	}

	/**
	 * Implementors of BaseResource should override this method as necessary to
	 * do any post-processing logic after the entity has been created
	 * 
	 * provided entity.
	 * 
	 * @param user
	 * @param parentSpaceId
	 * @param addedEntity
	 */
	protected void afterAdd(User user, T addedEntity) {
		// Override in sub-class
	}

	@PUT
	@Path("/{uuid}")
	@Timed
	public Response update(@Auth(required = false) User user,
			@PathParam("uuid") String uuid, @Valid T providedEntity) {

		LOG.debug("Trying to update Entity " + providedEntity);

		if (providedEntity == null) {
			LOG.error("No entity was provided as part of the request payload");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// NOTE: It is left to the sub-classing resource to validate/use
		// the parentSpaceId. It is included here since most of our updates
		// SHOULD use a filter by LearningSpace

		// Check the uuid
		LOG.debug("uuid = " + uuid + " providedEntity.uuid = "
				+ providedEntity.getUuid());
		if (!uuid.equals(providedEntity.getUuid())) {
			LOG.error("UUID in path and in provided topic JSON do not match!!");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// Make sure the topic already exists
		T existingEntity = repository.findOne(uuid.toString());
		if (existingEntity == null) {
			LOG.error("Provided uuid does not match any existing Entities");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// perform any pre-processing logic prior to updating the entity
		beforeUpdate(user, existingEntity, providedEntity);

		// Copy attributes from provided entity into existing entity
		T entityToUpdate;
		try {
			entityToUpdate = performEntityUpdate(existingEntity, providedEntity);
		} catch (IllegalAccessException e) {
			LOG.error("Unable to update existing entity with property values from provided entity!!");
			throw new WebApplicationException(e, Status.BAD_REQUEST);
		} catch (InvocationTargetException e) {
			LOG.error("Unable to update existing entity with property values from provided entity!!");
			throw new WebApplicationException(e, Status.BAD_REQUEST);
		}

		// Everything is OK - save/update the entity
		T updatedEntity = repository.save(entityToUpdate);

		// perform any post-processing
		updatedEntity = afterUpdate(updatedEntity, providedEntity);

		// finally, update the index
		index.addToIndex(updatedEntity);

		LOG.debug("UPDATED Entity => [" + updatedEntity.getUuid() + " : "
				+ updatedEntity.getVersion() + "]");

		return Response
				.created(
						UriBuilder.fromPath("/{uuid}").build(
								updatedEntity.getUuid())).entity(updatedEntity)
				.build();
	}

	/**
	 * Implementors of BaseResource should override this method as necessary to
	 * do any pre-processing before update, including any AuthZ checks
	 * 
	 * provided entity.
	 * 
	 * @param user
	 * @param existingEntity
	 * @param providedEntity
	 */
	protected void beforeUpdate(User user, T existingEntity, T providedEntity) {
		// Override in sub-class
	}

	/**
	 * Implementors of BaseResource should override this method as necessary to
	 * provide logic to update the existing entity in the database with
	 * attributes from the provided entity.
	 * 
	 * 
	 * @param existingEntity
	 * @param providedEntity
	 * @return
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	protected T performEntityUpdate(T existingEntity, T providedEntity)
			throws IllegalAccessException, InvocationTargetException {
		// Copy all non-null attributes from the provided entity over to the
		// existing entity
		// Note that in addition to nulls, the UUID is excluded from copy
		// Note also that empty attributes (lists, strings) are not treated as
		// nulls and are copied
		CustomBeanUtilBean beanUtils = new CustomBeanUtilBean();
		beanUtils.copyProperties(existingEntity, providedEntity);
		return existingEntity;
	}

	/**
	 * Implementors of BaseResource should override this method as necessary to
	 * do after update logic to update other entities. provided entity.
	 * 
	 * 
	 * @param existingEntity
	 * @param providedEntity
	 * @return
	 */
	protected T afterUpdate(T existingEntity, T providedEntity) {
		return existingEntity;
	}

	@DELETE
	@Path("/{uuid}")
	@Timed
	public Response delete(@Auth(required = false) User user, @PathParam("uuid") String uuid) {

		if (uuid == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		beforeDelete(user, uuid);

		T entityToRemove = repository.findOne(uuid);

		if (entityToRemove != null) {
			repository.delete(uuid);
			index.removeFromIndex(entityToRemove);
		} else {
			// Cannot delete non-existent entity
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// HTTP 204 No Content: The server successfully processed the request,
		// but is not returning any content
		return Response.noContent().build();
	}

	/**
	 * Pre-processing prior to delete, including any AuthZ checks
	 * 
	 * @param user
	 * @param uuid
	 */
	private void beforeDelete(User user, String uuid) {
		// TODO implemented in subclasses
	}
}
