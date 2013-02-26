package com.pragyasystems.knowledgeHub;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;



import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.foobar.todos.SetupService;
import com.foobar.todos.constants.Constants;
import com.pragyasystems.knowledgeHub.api.security.Group;
import com.pragyasystems.knowledgeHub.api.security.User;
import com.pragyasystems.knowledgeHub.api.space.LearningSpace;
import com.pragyasystems.knowledgeHub.db.group.GroupRepository;
import com.pragyasystems.knowledgeHub.db.space.SpaceRepository;
import com.pragyasystems.knowledgeHub.db.user.UserRepository;
import com.pragyasystems.knowledgeHub.resources.space.LearningSpaceUtil;

@Test(groups = { "checkintest" })
public class SetupServiceTest {

	private SpaceRepository mockSpaceRepository;
	private UserRepository mockUserRepository;
	private GroupRepository mockGroupRepository;
	private SetupService service;
	private LearningSpaceUtil mockLearningSpaceUtil;

    @BeforeMethod
    public void before() {
    	mockSpaceRepository = mock(SpaceRepository.class);
    	mockUserRepository = mock(UserRepository.class);
    	mockGroupRepository = mock(GroupRepository.class);
    	mockLearningSpaceUtil = mock(LearningSpaceUtil.class);
    	service = new SetupService(
    			mockSpaceRepository, mockUserRepository,
    			mockGroupRepository, mockLearningSpaceUtil);
    
    }
	@SuppressWarnings("unchecked")
	@Test
	public void start_whenRootExists() throws Exception {
		final LearningSpace space = new LearningSpace();
		space.setUuid("511846484800016990f86233");
		space.setVersion("1.3");
		space.setTitle(Constants.ROOT_LEARNING_SPACE_TITLE);
		space.setDescription("test description");
		List<LearningSpace> learningSpaces = new ArrayList<LearningSpace>();
		learningSpaces.add(space);
		when(mockSpaceRepository.findByTitle(any(String.class))).thenReturn(learningSpaces);
		service.start();
		verify(mockUserRepository, times(0)).save(any(User.class));
		verify(mockGroupRepository, times(0)).save(any(Group.class));
		verify(mockSpaceRepository, times(0)).save(any(LearningSpace.class));

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void start_whenRootDoesNotExists() throws Exception {
		final LearningSpace space = new LearningSpace();
		space.setUuid("511846484800016990f86233");
		space.setVersion("1.3");
		space.setTitle(Constants.ROOT_LEARNING_SPACE_TITLE);
		space.setDescription("test description");

		final User superAdmin = new User();
		superAdmin.setUuid("511846484800016990f86233");
		superAdmin.setFirstName("FAKE");
		superAdmin.setLastName("ADMIN");
		superAdmin.setUsername("FAKE-Admin");
		superAdmin.setPassword("Fake_Pwd");
		
		Group group = new Group();
		group.setUuid("511846484800016990f86233");
		group.setName("FAKE-GROUP");
		group.addUser(superAdmin);
		
		when(mockSpaceRepository.findByTitle(any(String.class))).thenReturn(null);
		when(mockUserRepository.save(any(User.class))).thenReturn(superAdmin);
		when(mockGroupRepository.save(any(Group.class))).thenReturn(group);
		when(mockSpaceRepository.save(any(LearningSpace.class))).thenReturn(space);
		doNothing().when(mockLearningSpaceUtil).createGroupsForAllRoles(any(LearningSpace.class));
		service.start();
		verify(mockUserRepository, times(2)).save(any(User.class));
		verify(mockGroupRepository, times(1)).save(any(Group.class));
		verify(mockSpaceRepository, times(1)).save(any(LearningSpace.class));
		verify(mockLearningSpaceUtil, times(1)).createGroupsForAllRoles(any(LearningSpace.class));

		
	}
}