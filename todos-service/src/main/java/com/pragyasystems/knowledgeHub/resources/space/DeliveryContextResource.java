package com.pragyasystems.knowledgeHub.resources.space;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.foobar.todos.constants.Constants;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.security.AccessControlEntry;
import com.pragyasystems.knowledgeHub.api.security.Principal;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.DeliveryContext;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.space.DeliveryContextRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResource;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;

@Component
@Path("/deliverycontext")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DeliveryContextResource extends BaseResource<DeliveryContext> {

	private static final Logger LOG = LoggerFactory.getLogger(DeliveryContextResource.class);
	private DeliveryContextRepository deliveryContextRepository;
	private LearningSpaceUtil learningSpaceUtil;
	private UserRepository userRepository;

	@Autowired
	public DeliveryContextResource(
			DeliveryContextRepository deliveryContextRepository,
			ElasticSearchIndex<DeliveryContext> elasticSearchIndexForDeliveryContext,
			LearningSpaceUtil learningSpaceUtil,
			UserRepository userRepository) {
		super(deliveryContextRepository, elasticSearchIndexForDeliveryContext);
		this.deliveryContextRepository = deliveryContextRepository;
		this.learningSpaceUtil = learningSpaceUtil;
		this.userRepository = userRepository;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pragyasystems.knowledgeHub.resources.BaseResource#beforeAdd(com.
	 * pragyasystems.knowledgeHub.api.security.User, java.lang.String,
	 * com.pragyasystems.knowledgeHub.api.Entity)
	 */
	@Override
	protected void beforeAdd(User user, String parentSpaceId,
			DeliveryContext providedDeliveryContext) {
		providedDeliveryContext
				.setCategory(Constants.LS_CATEGORY_DELIVERY_CONTEXT);
		LearningSpace ancestor = deliveryContextRepository
				.findOne(parentSpaceId);
		providedDeliveryContext.setAncestor(ancestor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.pragyasystems.knowledgeHub.resources.BaseResource#afterAdd(com.
	 * pragyasystems.knowledgeHub.api.security.User, java.lang.String,
	 * com.pragyasystems.knowledgeHub.api.Entity)
	 */
	@Override
	protected void afterAdd(User user, String parentSpaceId,
			DeliveryContext addedDeliveryContext) {
		learningSpaceUtil.createGroupsForAllRoles(addedDeliveryContext);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.pragyasystems.knowledgeHub.resources.BaseResource#performEntityUpdate
	 * (com.pragyasystems.knowledgeHub.api.Entity,
	 * com.pragyasystems.knowledgeHub.api.Entity)
	 */
	@Override
	public DeliveryContext performEntityUpdate(DeliveryContext existingEntity,
			DeliveryContext providedEntity) throws IllegalAccessException,
			InvocationTargetException {
		learningSpaceUtil.setGroupNames(existingEntity, providedEntity);
		return super.performEntityUpdate(existingEntity, providedEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.pragyasystems.knowledgeHub.resources.BaseResource#afterUpdate(com
	 * .pragyasystems.knowledgeHub.api.Entity,
	 * com.pragyasystems.knowledgeHub.api.Entity)
	 */
	@Override
	public DeliveryContext afterUpdate(DeliveryContext existingEntity,
			DeliveryContext providedEntity) {
		learningSpaceUtil.updateGroupNames(existingEntity);
		return existingEntity;
	}

	@GET
	@Timed
	@Path("/user")
	public List<LearningSpace> getEnrolledDeliveryContextForUser(
			@QueryParam("userUuid") @DefaultValue("NONE") String userUuid) {
		Iterable<LearningSpace> spaceList = learningSpaceUtil
				.getEnrolledLearningSpaceForUser(userUuid);
		List<LearningSpace> deliveryContextList = new ArrayList<LearningSpace>();
		for (LearningSpace space : spaceList) {
			if (space.getCategory().equals(
					Constants.LS_CATEGORY_DELIVERY_CONTEXT)) {
				DeliveryContext deliveryContext = (DeliveryContext)space;

				//show only current and past dated DC's
				if(Long.parseLong(deliveryContext.getYear()) <= new Date().getTime()){
					deliveryContextList.add(deliveryContext);
				}
			}
		}
		return deliveryContextList;
	}

	@GET
	@Timed
	@Path("/parentspace")
	public List<DeliveryContext> findByParentId(
			@QueryParam("parentSpaceId") @DefaultValue("NONE") String parentSpaceId,
			@QueryParam("page") @DefaultValue("0") IntParam page,
			@QueryParam("count") @DefaultValue("20") IntParam count) {
		LearningSpace parentSpace = deliveryContextRepository
				.findOne(parentSpaceId);
		Pageable pageable = new PageRequest(page.get(), count.get());
		return deliveryContextRepository.findByCategoryAndAncestor(
				Constants.LS_CATEGORY_DELIVERY_CONTEXT, parentSpace, pageable);
	}
	
	/**
	 * TODO Deepak This required better implementation. JUST ADDED TO SUPPORT
	 * CURRENT IMPLEMENTATION THIS IS LASO MISSING LOGIC WHEN UN-ENROLLING
	 * 
	 * @param user
	 * @param deliveryContextId
	 * @param userId
	 * @return
	 */
	@PUT
	@Path("/{deliveryContextId}/enroll/{userId}")
	@Timed
	public Response enroll(@Auth User user,
			@PathParam("deliveryContextId") String deliveryContextId,
			@PathParam("userId") String userId) {
		// multiple enrollment
		String[] userIdArray = { userId };
		if (userId.contains(",")) {
			userIdArray = userId.split(",");
		}
		LearningSpace deliveryContext = deliveryContextRepository
				.findOne(deliveryContextId);
		for (String id : userIdArray) {
			User usertoEnroll = userRepository.findOne(id);
			learningSpaceUtil.enrollToLearningSpace(deliveryContext,
					usertoEnroll);
		}
		return Response
				.created(
						UriBuilder.fromPath("/{uuid}").build(
								deliveryContext.getUuid()))
				.entity(deliveryContext).build();
	}

	@PUT
	@Path("/updatePermision/{userPermission}/{uuid}")
	@Timed
	public Response updateUserPermission(@Auth User user,
			@PathParam("uuid") String uuid,
			@PathParam("userPermission") String userPermission) {
		// Get DC from database from uuid
		DeliveryContext deliveryContext = deliveryContextRepository
				.findOne(uuid);
		if (deliveryContext != null && userPermission != null) {
			// get all acelist from selected DC
			List<AccessControlEntry> accessControlEntries = deliveryContext
					.getACEList();
			for (AccessControlEntry oldAccessControlEntry : accessControlEntries) {
				if (oldAccessControlEntry != null) {
					String studentGroupInDC = deliveryContext.getTitle()
							+ Constants.GROUP_LS_STUDENT_SUFFIX;
					Principal principal = oldAccessControlEntry.getPrincipal();

					// select studentGroup from all aceList
					if (principal != null
							&& principal.getName().equalsIgnoreCase(
									studentGroupInDC)) {
						AccessControlEntry newAccessControlEntry = new AccessControlEntry();
						newAccessControlEntry
								.setPrincipal(oldAccessControlEntry
										.getPrincipal());
						newAccessControlEntry.setRead(oldAccessControlEntry
								.canRead());
						if (userPermission.equals("true")) {
							newAccessControlEntry.setWrite(Boolean.TRUE);
						} else {
							newAccessControlEntry.setWrite(Boolean.FALSE);
						}
						deliveryContext.updateACE(oldAccessControlEntry,
								newAccessControlEntry);
						break;
					}
				}
			}
			deliveryContextRepository.save(deliveryContext);
		} else {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		return Response
				.created(
						UriBuilder.fromPath("/{uuid}").build(
								deliveryContext.getUuid()))
				.entity(deliveryContext).build();
	}
}