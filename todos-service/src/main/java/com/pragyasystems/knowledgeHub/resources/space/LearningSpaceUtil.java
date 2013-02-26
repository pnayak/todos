package com.pragyasystems.knowledgeHub.resources.space;

import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.foobar.todos.constants.Constants;
import com.foobar.todos.elasticsearch.ElasticSearchIndex;
import com.google.common.collect.Lists;
import com.pragyasystems.knowledgeHub.api.security.AccessControlEntry;
import com.pragyasystems.knowledgeHub.api.security.Group;
import com.pragyasystems.knowledgeHub.api.security.Principal;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.group.GroupRepository;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;


public class LearningSpaceUtil {
	
	private static final Logger LOG = LoggerFactory.getLogger(LearningSpaceUtil.class);
	@Autowired
	private  SpaceRepository spaceRepository;
	@Autowired
	private  ElasticSearchIndex<LearningSpace> elasticSearchIndexForSpace;
	@Autowired
	private  UserRepository userRepository;
	@Autowired
	private  GroupRepository groupRepository;

	/**
	 * PRAGYA_ROOT_LEARNING_SPACE to be created by DBScript/Service. 
	 * @param spaceRepository
	 * @return
	 */
	public LearningSpace getPragyaROOTLS() {
		return spaceRepository.findByTitle(Constants.ROOT_LEARNING_SPACE_TITLE).get(0);
	}
	
	/**
	 * This method returns all learning spaces (all types, LS, DC, INSTITUTION)
	 * which user have access(enrolled)
	 * @param userUuid
	 * @return
	 */
	public Iterable<LearningSpace> getEnrolledLearningSpaceForUser(String userUuid) {
		Iterable<LearningSpace> spaceList = Lists.newArrayList();
		User user = userRepository.findOne(userUuid);
			if (user.getGroups().isEmpty()) {
				// User does not belong to any groups.
				// Just query for LearningSpaces with ACE's for the users UUID
				ObjectId objectId = new ObjectId(userUuid);
				spaceList = spaceRepository.findByAceListPrincipalUuid(objectId);
			} else {
				// Query for LearningSpace with ACE's for users UUID and group
				// UUID's
				List<ObjectId> listOfIds = Lists.newArrayList();

				// add all the group uuid's
				List<String> groupUuids = user.getGroups();
				for (String groupUuid : groupUuids) {
					ObjectId objectId = new ObjectId(groupUuid);
					listOfIds.add(objectId);
				}
				// add the user uuid
				listOfIds.add(new ObjectId(userUuid));

				ObjectId[] objectIds = listOfIds.toArray(new ObjectId[listOfIds
						.size()]);
				spaceList = spaceRepository.findByAceListPrincipalUuidIn(objectIds);
			}
		return spaceList;
	}

	
	/**
	 * This method will enroll user to institution and institution public learning space.
	 * @param institution
	 * @param user
	 */
	public void enrollToInstitution(LearningSpace institution, User user){
		//enroll to Institution
		enrollToLearningSpace(institution, user);
		enrollToInstitutionPublicLS(institution, user);
	}
	
	/**
	 * This method will enroll user to any Learning Space (LS, DC, INSTITUTION)
	 * users added to appropriate group based on role.
	 * @param learningSpace
	 * @param user
	 */
	public void enrollToLearningSpace(LearningSpace learningSpace, User user){
		Group group;
		if(Constants.ROLE_INSTRUCTOR.equals(user.getRoles().get(0).getRole())){
			// Enroll to Institution
			String insGroupName = learningSpace.getTitle() + Constants.GROUP_LS_INSTRUCTOR_SUFFIX;
			group = groupRepository.findByName(insGroupName).get(0);
			enrollUser(group, user);
		}
		if(Constants.ROLE_ADMIN.equals(user.getRoles().get(0).getRole())){
			String insGroupName = learningSpace.getTitle() + Constants.GROUP_LS_ADMIN_SUFFIX;
			group = groupRepository.findByName(insGroupName).get(0);
			enrollUser(group, user);
		}
		if(Constants.ROLE_STUDENT.equals(user.getRoles().get(0).getRole())){
			String insGroupName = learningSpace.getTitle() + Constants.GROUP_LS_STUDENT_SUFFIX;
			group = groupRepository.findByName(insGroupName).get(0);
			enrollUser(group, user);
		}
	}
	
	/**
	 * This method will enroll user to institution public learning space.
	 * @param institution
	 * @param user
	 */
	public void enrollToInstitutionPublicLS(LearningSpace institution, User user){
		// Enroll to Institution public learning Space.
		Group institutionPublicLSGroup;
		if(Constants.ROLE_INSTRUCTOR.equals(user.getRoles().get(0).getRole())){
			String insPublicLSGroupName = institution.getTitle() + Constants.INSTITUTION_PUBLIC_LS_SUFFIX + Constants.GROUP_LS_INSTRUCTOR_SUFFIX;
			institutionPublicLSGroup = groupRepository.findByName(insPublicLSGroupName).get(0);
			enrollUser(institutionPublicLSGroup, user);
		}
		if(Constants.ROLE_ADMIN.equals(user.getRoles().get(0).getRole())){
			String insPublicLSGroupName = institution.getTitle() + Constants.INSTITUTION_PUBLIC_LS_SUFFIX + Constants.GROUP_LS_ADMIN_SUFFIX;
			institutionPublicLSGroup = groupRepository.findByName(insPublicLSGroupName).get(0);
			enrollUser(institutionPublicLSGroup, user);
		}
		if(Constants.ROLE_STUDENT.equals(user.getRoles().get(0).getRole())){
			String insPublicLSGroupName = institution.getTitle() + Constants.INSTITUTION_PUBLIC_LS_SUFFIX + Constants.GROUP_LS_STUDENT_SUFFIX;
			institutionPublicLSGroup = groupRepository.findByName(insPublicLSGroupName).get(0);
			enrollUser(institutionPublicLSGroup, user);
		}
	}

	/**
	 * This method enroll user to appropriate group of
	 * PRAGAY PUBLIC LEARNING SPACE.
	 * @param user
	 */
	public void enrollToPragyaPublicLearningSpace(User user){
		String pragyaPLSGroupName = Constants.PRAGYA_PUBLIC_LEARNING_SPACE_TITLE;
		if(Constants.ROLE_INSTRUCTOR.equals(user.getRoles().get(0).getRole())){
			pragyaPLSGroupName +=Constants.GROUP_LS_INSTRUCTOR_SUFFIX;
		}else if(Constants.ROLE_ADMIN.equals(user.getRoles().get(0).getRole())){
			pragyaPLSGroupName +=Constants.GROUP_LS_ADMIN_SUFFIX;
		}else if(Constants.ROLE_STUDENT.equals(user.getRoles().get(0).getRole())){
			pragyaPLSGroupName +=Constants.GROUP_LS_STUDENT_SUFFIX;
		}
		
		Group groupToEnroll = groupRepository.findByName(pragyaPLSGroupName).get(0);
		enrollUser(groupToEnroll, user);
	}
	
	/**
	 * This method create private LS for user and give access
	 * Also set this learning space as user's personal public space.
	 * @param user
	 * @param parentSpace
	 * @return
	 */
	public LearningSpace createPrivateLearningSpace(User user, LearningSpace parentSpace){
		LearningSpace privateLearningSpace = new LearningSpace();
		privateLearningSpace.setTitle(user.getUsername().toUpperCase()+ Constants.USER_PERSONAL_LS_SUFFIX);
		privateLearningSpace.setAncestor(parentSpace);
		privateLearningSpace.setCategory(Constants.LS_CATEGORY_USER_PRIVATE);
		privateLearningSpace.addACE(getACE(user,Boolean.TRUE, Boolean.TRUE));
		privateLearningSpace = spaceRepository.save(privateLearningSpace);
		user.setPersonalLearningSpaceUUID(privateLearningSpace.getUuid());
		userRepository.save(user);
		return privateLearningSpace;
	}

	/**
	 * create public LS as child LS for Institution with Admin, Instuctor, student groups 
	 * @param parentSpace
	 */
	public void createInstitutePublicLearningSpace(LearningSpace parentSpace){
		LearningSpace childLS = new LearningSpace();
		childLS.setTitle(parentSpace.getTitle()+Constants.INSTITUTION_PUBLIC_LS_SUFFIX);
		childLS.setAncestor(parentSpace);
		childLS.setCategory(Constants.LS_CATEGORY_INSTITUTION_PUBLIC);
		childLS = spaceRepository.save(childLS);
		createGroupsForAllRoles(childLS);
	}
	/**
	 * create all three type of group fot LS
	 * 
	 * @param learningSpace
	 */
	public void createGroupsForAllRoles(LearningSpace learningSpace) {
		// Add Instructor group
		String instructorGroupName = learningSpace.getTitle() + Constants.GROUP_LS_INSTRUCTOR_SUFFIX;
		AccessControlEntry instructorACE = getACE(Boolean.TRUE, Boolean.TRUE, instructorGroupName);
		learningSpace.addACE(instructorACE);

		// Add Admin group
		String adminGroupName = learningSpace.getTitle() + Constants.GROUP_LS_ADMIN_SUFFIX;
		AccessControlEntry adminACE = getACE(Boolean.TRUE, Boolean.TRUE, adminGroupName);
		learningSpace.addACE(adminACE);

		// Add Student group
		String studentGroupName = learningSpace.getTitle() + Constants.GROUP_LS_STUDENT_SUFFIX;
		AccessControlEntry studentACE = getACE(Boolean.TRUE, Boolean.FALSE, studentGroupName);
		learningSpace.addACE(studentACE);
		spaceRepository.save(learningSpace);
	}
	/**
	 * TODO We do not need this, we DO NOT allow changing the tthe title.
	 * Call by resource, when institution is updated.
	 * @param parentSpace
	public void updateInstitutionPublicLearningSpace(LearningSpace parentSpace){
		Pageable pageable = new PageRequest(0, 1);
		LearningSpace institutionLearningSpace = spaceRepository.findByCategoryAndAncestor(Constants.LS_CATEGORY_INSTITUTION_PUBLIC, parentSpace, pageable).get(0);
		String newTitle = parentSpace.getTitle()+Constants.INSTITUTION_PUBLIC_LS_SUFFIX;
		LearningSpace temp = new LearningSpace();
		temp.setTitle(newTitle);
		setGroupNames(institutionLearningSpace, temp);
		spaceRepository.save(institutionLearningSpace);
		updateGroupNames(institutionLearningSpace);
	}
	 */
	
	/**
	 * 
	 * This method get called when updating delivery context, before update change the group names.
	 * to use the new title
	 * @param existing
	 * @param updated
	 */
	public void setGroupNames(LearningSpace existing, LearningSpace updated){
		if(!existing.getTitle().equals(updated.getTitle())){
			String instructorGroupName = existing.getTitle() + Constants.GROUP_LS_INSTRUCTOR_SUFFIX;
			String adminGroupName = existing.getTitle() + Constants.GROUP_LS_ADMIN_SUFFIX;
			String studentGroupName = existing.getTitle() + Constants.GROUP_LS_STUDENT_SUFFIX;

			for(AccessControlEntry ace: existing.getACEList()){
				if(ace.getPrincipal().getName().equals(instructorGroupName)){
					((Group) ace.getPrincipal()).setName(updated.getTitle() + Constants.GROUP_LS_INSTRUCTOR_SUFFIX);
				}else if(ace.getPrincipal().getName().equals(adminGroupName)){
					((Group) ace.getPrincipal()).setName(updated.getTitle() + Constants.GROUP_LS_ADMIN_SUFFIX);
				}else if(ace.getPrincipal().getName().equals(studentGroupName)){
					((Group) ace.getPrincipal()).setName(updated.getTitle() + Constants.GROUP_LS_STUDENT_SUFFIX);
				}
			}
		}
	}
	/**
	 * This method called after DeliveryContext is updated, to update the group names
	 * to match with new title.
	 * @param existing
	 */
	public void updateGroupNames(LearningSpace existing){
		for(AccessControlEntry ace: existing.getACEList()){
			String newGroupName = ace.getPrincipal().getName();
			Group currentGroup = groupRepository.findOne(ace.getPrincipal().getUuid());
			currentGroup.setName(newGroupName);
			groupRepository.save(currentGroup);
		}
	}
	
	/**
	 * This method will create a group with ACE and return ACE back
	 * @param read
	 * @param write
	 * @param groupName
	 * @return
	 */
	private AccessControlEntry getACE(Boolean read, Boolean write, String groupName){
		Group group = new Group();
		group.setName(groupName);
		group = groupRepository.save(group);
		return getACE(group, read, write);
	}
	/**
	 * Create ACE with the permission and add provided principal (group/user).
	 * @param p
	 * @param read
	 * @param write
	 * @return
	 */
	private AccessControlEntry getACE(Principal p, Boolean read, Boolean write){
		AccessControlEntry ace = new AccessControlEntry();
		ace.setPrincipal(p);
		ace.setRead(read);
		ace.setWrite(write);
		return ace;
		
	}
	/**
	 * Add User to group and group to user and update.
	 * @param group
	 * @param user
	 */
	private void enrollUser(Group group, User user){
		group.addUser(user);
		user.addGroup(group);
		userRepository.save(user);
		groupRepository.save(group);
	}
	
}
