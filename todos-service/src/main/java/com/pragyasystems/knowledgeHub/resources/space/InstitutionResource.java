package com.pragyasystems.knowledgeHub.resources.space;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.foobar.todos.constants.Constants;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.Institution;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.space.InstitutionRepository;
import com.pragyasystems.knowledgeHub.resources.BaseResource;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;

@Component
@Path("/institution")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InstitutionResource extends BaseResource<Institution> {
	
	private static final Logger LOG = LoggerFactory.getLogger(InstitutionResource.class);
	private InstitutionRepository institutionRepository;
	private LearningSpaceUtil learningSpaceUtil;
	
	
	@Autowired
	public InstitutionResource(InstitutionRepository institutionRepository,
			ElasticSearchIndex<Institution> elasticSearchIndexForInstitution,
			LearningSpaceUtil learningSpaceUtil) {
		super(institutionRepository, elasticSearchIndexForInstitution);
		this.institutionRepository = institutionRepository;
		this.learningSpaceUtil = learningSpaceUtil;
	}
	/**
	 * This method only be access by super admin user
	 * to see all the institution
	 */
	@Override
	@GET
	@Timed
	public List<Institution> findAll(@Auth User user,
			@QueryParam("parentSpaceId") String parentSpaceId,
			@QueryParam("page") @DefaultValue("0") IntParam page,
			@QueryParam("count") @DefaultValue("20") IntParam count) {
		return institutionRepository.findByCategory(Constants.LS_CATEGORY_INSTITUTION);
	}	

	@Override
	protected void beforeAdd(User user, String parentSpaceId, Institution providedInstitution) {
		checkUniqueness(providedInstitution);
        providedInstitution.setCategory(Constants.LS_CATEGORY_INSTITUTION);
		LearningSpace ancestor = learningSpaceUtil.getPragyaROOTLS();
		providedInstitution.setAncestor(ancestor);
	}
	/**
	 * 1. Create groups for all roles
	 * 2. Create public learning space for the institution.
	 * 3. Create groups for all roles for institution learning space.
	 * 
	 */
	@Override
	protected void afterAdd(User user, String parentSpaceId, Institution createdInstitution) {
		learningSpaceUtil.createGroupsForAllRoles(createdInstitution);
		learningSpaceUtil.createInstitutePublicLearningSpace(createdInstitution);
	}
	
	/**
	 * check all pre-condition when updating.
	 * set the new group names, so it will be use when updating teh group names
	 * after update of institution.
	 */
	@Override
	public Institution performEntityUpdate(Institution existingInstitution, Institution providedInstitution) throws IllegalAccessException, InvocationTargetException {
		if(isUniquenessChange(existingInstitution, providedInstitution)){
			checkUniqueness(providedInstitution);
		}
		// TODO We do not need this, we DO NOT allow changing the tthe title.
		//learningSpaceUtil.setGroupNames(existingInstitution, providedInstitution);
		return super.performEntityUpdate(existingInstitution, providedInstitution);
	}
	/**
	 * TODO We do not need this, we DO NOT allow changing the tthe title.
	 * 1. update the group names
	 * 2. update the name of institution public learning space
	 * 3. update the name of groups of institution public learning space 
	@Override
	public Institution afterUpdate(Institution existingEntity,
			Institution providedEntity) {
		// update the name of groups
		learningSpaceUtil.updateGroupNames(existingEntity);
		// update the name of Institution public learning space and groups
		learningSpaceUtil.updateInstitutionPublicLearningSpace(existingEntity);
		return existingEntity;
	}
	 */

	private boolean isUniquenessChange(Institution existingInstitution, Institution providedInstitution){
		boolean titleUnChanged = existingInstitution.getTitle().equals(providedInstitution.getTitle());
		boolean addressUnChanged = existingInstitution.getAddress().equals(providedInstitution.getAddress());
		return !(titleUnChanged && addressUnChanged);
	}
	/**
	 * This method check if institution with
	 * name and location combination already exists.
	 * @param providedInstitution
	 */
	private void checkUniqueness(Institution providedInstitution) {
		List<Institution> institutionList = institutionRepository.findByCategoryAndTitleAndAddress(Constants.LS_CATEGORY_INSTITUTION, 
				providedInstitution.getTitle(), providedInstitution.getAddress());
		if(institutionList != null && institutionList.size() > 0){
			LOG.debug("Institute Already Exists : Invalid request...");
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
	}
}
