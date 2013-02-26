package com.foobar.todos.constants;

public interface Constants {

	//	Define constants for learning space categories.
	String LS_CATEGORY_DELIVERY_CONTEXT = "DeliveryContext";
	String LS_CATEGORY_INSTITUTION = "Institution";
	String LS_CATEGORY_USER_PRIVATE = "User_Private";
	String LS_CATEGORY_INSTITUTION_PUBLIC = "Institute_Public";
	String LS_CATEGORY_LEARNING_SPACE = "LearningSpace";
	
	// 	Define Root Learning Space Name for Pragya.
	String ROOT_LEARNING_SPACE_TITLE = "Pragya Root Learning Space";
	String PRAGYA_PUBLIC_LEARNING_SPACE_TITLE = "Pragya Public Learning Space";
	
	//	Define principal type constants.
	String PRINCIPAL_USER = "USER";
	String PRINCIPAL_GROUP = "GROUP";
	
	// Define Institution Public Learning Space name Prefix and Suffix.
	String INSTITUTION_PUBLIC_LS_SUFFIX = " Public Learning Space";
	
	//	Define Learning Space default group name Prefix and Suffix.
	String GROUP_LS_DEFAULT_SUFFIX = "_USERS";
	String GROUP_LS_INSTRUCTOR_SUFFIX = "_INSTRUCTORS";
	String GROUP_LS_STUDENT_SUFFIX = "_STUDENTS";
	String GROUP_LS_ADMIN_SUFFIX = "_ADMINISTRATORS";
	
	//	Define Personal Learning Space Prefix and Suffix.
	String USER_PERSONAL_LS_SUFFIX = " Private Learning Space";
	
	//	Define constants for user roles.
	String ROLE_INSTRUCTOR = "Instructor";
	String ROLE_STUDENT = "Student";
	String ROLE_ADMIN = "Admin";
	String ROLE_SUPER_ADMIN = "SuperAdmin";
	
	//	Learning Object Types.
	String LO_TYPE_CONTENT = "LearningContent";
	String LO_TYPE_TOPIC = "LearningTopic";
	
	//	UserFeedback constants.
	String FEEDBACK_LIKED = "LIKED";
	String FEEDBACK_DISLIKED = "DISLIKED";	
	String FEEDBACK_HELPNEEDED = "HELPNEEDED";
	
	//	Watch count constant for learning object.
	String LEARNING_OBJECT_WATCHCOUNT = "WATCHCOUNT";
	
}
