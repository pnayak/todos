/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.user;

import static com.pragyasystems.knowledgeHub.resources.user.UserUtils.hashPassword;
import static com.pragyasystems.knowledgeHub.resources.user.UserUtils.passwordValid;
import static com.pragyasystems.knowledgeHub.resources.user.UserUtils.validatePasswordUpdate;

import java.io.InputStream;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.foobar.todos.constants.Constants;
import com.foobar.todos.db.security.AccessTokenRepository;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.google.common.collect.Lists;
import com.pragyasystems.knowledgeHub.api.security.AccessToken;
import com.pragyasystems.knowledgeHub.api.security.Group;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.security.UserPreferences;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.group.GroupRepository;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.space.LearningSpaceUtil;
import com.pragyasystems.knowledgeHub.resources.useruploader.UserCsvUploader;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.yammer.dropwizard.jersey.params.IntParam;
import com.yammer.metrics.annotation.Timed;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         The resource that provides access to learning content
 * 
 */
@Component
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

	private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);
	private UserRepository userRepository;
	private SpaceRepository spaceRepository;
	private ElasticSearchIndex<User> userIndex;
	private GroupRepository groupRepository;
	private AccessTokenRepository tokenRepository;
	private LearningSpaceUtil learningSpaceUtil;

	/**
	 * @param contentRepository
	 */
	@Autowired
	public UserResource(UserRepository userRepository,
			ElasticSearchIndex<User> elasticSearchIndexForUser,
			SpaceRepository spaceRepository, GroupRepository groupRepository,
			AccessTokenRepository tokenRepository,
			LearningSpaceUtil learningSpaceUtil) {
		super();
		this.userRepository = userRepository;
		this.userIndex = elasticSearchIndexForUser;
		this.spaceRepository = spaceRepository;
		this.groupRepository = groupRepository;
		this.tokenRepository = tokenRepository;
		this.learningSpaceUtil = learningSpaceUtil;
	}

	@GET
	@Path("/{uuid}")
	@Timed
	public User findOne(@PathParam("uuid") String uuid) {

		if (uuid == null) {
			throw new WebApplicationException(Status.NO_CONTENT);
		}

		User foundUser = userRepository.findOne(uuid);

		if (foundUser != null) {
			return foundUser;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@GET
	@Timed
	public Iterable<User> findAll(
			@QueryParam("count") @DefaultValue("20") IntParam count) {

		Iterable<User> userList = userRepository.findAll();

		if (userList != null) {
			return userList;
		} else {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@PUT
	@Path("/create/{learningSpaceUuid}")
	@Timed
	public Response add(
			@PathParam("learningSpaceUuid") String learningSpaceUuid,
			@Valid User providedUser) {

		if (providedUser == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// hash the user password. Will throw exception if provided password is
		// already hashed
		try {
			providedUser.setPassword(hashPassword(providedUser.getPassword()));
		} catch (Exception e) {
			throw new WebApplicationException(e, Status.BAD_REQUEST);
		}

		LearningSpace foundSpace = spaceRepository.findOne(learningSpaceUuid);
		if (foundSpace == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		User user = userRepository.save(providedUser);
		userIndex.addToIndex(user);
		// Enroll in Institution and Public LS
		learningSpaceUtil.enrollToInstitution(foundSpace, user);

		// Create Private LS for User
		if(isInstructorOrStudent(user)){
			learningSpaceUtil.createPrivateLearningSpace(user, foundSpace);
			// Enroll in Pragya Public learning Space
			learningSpaceUtil.enrollToPragyaPublicLearningSpace(user);
		}
		
		LOG.debug("Added User => [" + user.getUuid() + " : "
				+ user.getVersion() + "]");

		return Response
				.created(UriBuilder.fromPath("/{uuid}").build(user.getUuid()))
				.entity(user).build();
	}
	/**
	 * For now user can have only one role so just get the first element.
	 * @param user
	 * @return
	 */
	private boolean isInstructorOrStudent(User user){
		String userRole = user.getRoles().get(0).getRole();
		return (userRole.equalsIgnoreCase(Constants.ROLE_INSTRUCTOR)|| userRole.equalsIgnoreCase(Constants.ROLE_STUDENT));
	}
	
	
	@PUT
	@Path("/{uuid}")
	@Timed
	public Response update(@PathParam("uuid") String uuid,
			@Valid User providedUser) {

		if (providedUser == null || uuid == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		if (providedUser.getUuid() == null
				|| !providedUser.getUuid().equals(uuid)) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// We do not allow update for user that does not previously exist or
		// allow username change
		User storedUser = userRepository.findOne(uuid);
		if (storedUser == null
				|| !storedUser.getUsername().equals(providedUser.getUsername())) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		// If password is being changed, validate... throws exception
		// if you try to update with a already hashed password that is different
		// from the stored one
		try {
			validatePasswordUpdate(providedUser, storedUser);
		} catch (Exception e) {
			throw new WebApplicationException(e, Status.BAD_REQUEST);
		}
		
		
		
		if(storedUser != null){
			if(providedUser.getCity() != null)
				storedUser.setCity(providedUser.getCity());

			if(providedUser.getCountry() != null)
				storedUser.setCountry(providedUser.getCountry());

			if(providedUser.getDescription() != null)
				storedUser.setDescription(providedUser.getDescription());

			if(providedUser.geteMail() != null)
				storedUser.seteMail(providedUser.geteMail());

			if(providedUser.getFirstName() != null)
				storedUser.setFirstName(providedUser.getFirstName());

/*			if(providedUser.getGroups() != null)
				storedUser.setGroups(providedUser.getGroups());*/

			if(providedUser.getLanguage() != null)
				storedUser.setLanguage(providedUser.getLanguage());

			if(providedUser.getLastName() != null)
				storedUser.setLastName(providedUser.getLastName());

			if(providedUser.getMiddleName() != null)
				storedUser.setMiddleName(providedUser.getMiddleName());

			if(providedUser.getUserPreferences() != null)
				storedUser.setUserPreferences(providedUser.getUserPreferences());

		}

		User user = userRepository.save(storedUser);
		userIndex.addToIndex(user);

		LOG.debug("Updated User => [" + user.getUuid() + " : "
		                            + user.getVersion() + "]");

		return Response
		.created(UriBuilder.fromPath("/{uuid}").build(user.getUuid()))
		.entity(user).build();
	}

	@DELETE
	@Path("/{uuid}")
	@Timed
	public Response delete(@PathParam("uuid") String uuid) {

		if (uuid == null) {
			throw new WebApplicationException(Status.NO_CONTENT);
		}

		userRepository.delete(uuid);
		User userToRemove = new User(uuid);
		userIndex.removeFromIndex(userToRemove);

		// HTTP 204 No Content: The server successfully processed the request,
		// but is not returning any content
		return Response.noContent().build();
	}

	@POST
	@Timed
	@Path("/login")
	public User login(User providedUser) {

		if (providedUser == null || providedUser.getUsername() == null
				|| providedUser.getPassword() == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}

		LOG.debug("finding User with userName=> [" + providedUser.getUsername()
				+ "]");
		// This should contain only one item, username is unique.
		List<User> users = userRepository.findByUsername(providedUser
				.getUsername());
		try {
			if (users != null
					&& !users.isEmpty()
					&& passwordValid(providedUser.getPassword(), users.get(0)
							.getPassword())) {
				
				User authenticatedUser = users.get(0);
				
				// Create a new access token and save it
				AccessToken token = new AccessToken();
				token.setUser(authenticatedUser);
				AccessToken validToken = tokenRepository.save(token);
				
				// For now, return the token in the users password field
				authenticatedUser.setPassword(validToken.getUuid());
				
				return authenticatedUser;
			} else {
				throw new WebApplicationException(Status.UNAUTHORIZED);
			}
		} catch (Exception e) {
			throw new WebApplicationException(e, Status.UNAUTHORIZED);
		}
	}

	@GET
	@Path("/search")
	@Timed
	public List<User> findForParam(
			@QueryParam("parentSpaceId") @DefaultValue("NONE") String parentSpaceId,
			@QueryParam("role") @DefaultValue("NONE") String role) {

		if (parentSpaceId == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		LearningSpace foundSpace = spaceRepository.findOne(parentSpaceId
				.toString());

		if (foundSpace == null) {
			throw new WebApplicationException(Status.BAD_REQUEST);
		}
		List<String> userIdList = Lists.newArrayList();

		Group group = null;
		if(role.equalsIgnoreCase("NONE") || role.equalsIgnoreCase(Constants.ROLE_ADMIN)) {
			group = groupRepository.findByName(foundSpace.getTitle()+ Constants.GROUP_LS_ADMIN_SUFFIX).get(0);
			userIdList.addAll(group.getUserUuidList());
		}
		
		if(role.equalsIgnoreCase("NONE") || role.equalsIgnoreCase(Constants.ROLE_INSTRUCTOR)) {
			group = groupRepository.findByName(foundSpace.getTitle()+ Constants.GROUP_LS_INSTRUCTOR_SUFFIX).get(0);
			userIdList.addAll(group.getUserUuidList());
		}
		
		if(role.equalsIgnoreCase("NONE") || role.equalsIgnoreCase(Constants.ROLE_STUDENT)) {
			group = groupRepository.findByName(foundSpace.getTitle()+ Constants.GROUP_LS_STUDENT_SUFFIX).get(0);
			userIdList.addAll(group.getUserUuidList());
		}
	   ObjectId[] objectIds = convertListToObjectArray(userIdList);
	   
	   List<User> allUserObjectList = userRepository.findByUuidIn(objectIds);
	   
	 return allUserObjectList;
	}

	@SuppressWarnings("unchecked")
	@POST
	@Path("/csvUserUpload/{institutionID}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public String csvUserUpload(
			@PathParam("institutionID") String institutionId,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {
		StringBuilder stringBuilder = new StringBuilder();
		if (uploadedInputStream != null && fileDetail != null) {
			String name = fileDetail.getFileName();
			int lastIndex = name.lastIndexOf('.');
			String extension = name.substring(lastIndex);
			if (StringUtils.hasLength(extension)
					&& !extension.equalsIgnoreCase(".csv")) {
				stringBuilder.append("Please provide CSV file");
				return stringBuilder.toString();
			}
			UserCsvUploader csvUploader = new UserCsvUploader(uploadedInputStream);
			if (csvUploader != null) {
				Object[] obj = csvUploader.upload();
				List<String> uploadErrors = (List<String>) obj[0];
				List<User> userList = (List<User>) obj[1];

				for (User addUser : userList) {
					// creating user.
					Response response = add(institutionId, addUser);
					User createdUser = (User) response.getEntity();
					// The method below will enroll to institution and institution public LS
					LearningSpace institution = spaceRepository.findOne(institutionId);
					learningSpaceUtil.enrollToInstitution(institution, createdUser);
					// Also Enroll to Pragya Public LS
					learningSpaceUtil.enrollToPragyaPublicLearningSpace(createdUser);
				}

				for (String error : uploadErrors) {
					if (stringBuilder.length() > 0) {
						stringBuilder.append(",\n");
					}
					stringBuilder.append(error);
				}
				stringBuilder.append("\n\n");
				stringBuilder.append("Successful Records : "
						+ csvUploader.getRowsInserted() + "\n");
				stringBuilder.append("Unsuccessful Records : "
						+ uploadErrors.size());
			}
			return stringBuilder.toString();
		}
		return stringBuilder.append("No file provided").toString();
	}
	
	
	  @GET
	  @Timed
	  @Path("/findUsersByInstituteAndRole")
	  public List<User> findUsersByInstituteAndRole(
	    @QueryParam("instituteId") @DefaultValue("NONE") String instituteId,
	    @QueryParam("role") @DefaultValue("NONE") String role) {
	   
		   if (instituteId == null) {
		    LOG.warn(" Institute name not provided...");
		    throw new WebApplicationException(Status.BAD_REQUEST);
		   }
		   
		   LearningSpace foundSpace = spaceRepository.findOne(instituteId);
		   
		   List<User> userList = Lists.newArrayList();
		   Group institutionDefaultGroup = null;
		   // Deepak TODO We do not have any default group what we are doing here ?????????????
		   List<Group> groupList = groupRepository.findByName(foundSpace.getTitle() + Constants.GROUP_LS_DEFAULT_SUFFIX);
		   
		   for(Group nextGroup : groupList){
			    institutionDefaultGroup = nextGroup;
			    break;
		   }
		   
		   if(institutionDefaultGroup != null){
		   
			   List<String> userUUIDList = institutionDefaultGroup.getUserUuidList();
			   
			   ObjectId[] objectIds = convertListToObjectArray(userUUIDList);
			
			   // search for any space that matches any of the objectId's in
			   // the array
			 /*  spaceList = spaceRepository
			 .findByAceListPrincipalUuidIn(objectIds);*/
			   ///////////////////
			   List<User> userListforRole = userRepository.findByUuidIn(objectIds);
			   for (User user : userListforRole) {
				   if(user.getRoles().get(0).getRole().equalsIgnoreCase(role))
					   userList.add(user);
			   }
		   }
	   return userList;
	  }

	  
	/**
	 * 
	 * @param list
	 * @return
	 */
	private ObjectId[] convertListToObjectArray(List<String> list) {

		List<ObjectId> listOfIds = Lists.newArrayList();

		// add all the group uuid's
		for (String userUuid : list) {
			ObjectId objectId = new ObjectId(userUuid);
			listOfIds.add(objectId);
		}

		ObjectId[] objectIds = listOfIds
				.toArray(new ObjectId[listOfIds.size()]);
		return objectIds;
	}

}
