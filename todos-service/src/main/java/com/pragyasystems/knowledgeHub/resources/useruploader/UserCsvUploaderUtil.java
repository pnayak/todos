package com.pragyasystems.knowledgeHub.resources.useruploader;

import com.pragyasystems.knowledgeHub.api.security.Role;
import com.pragyasystems.knowledgeHub.api.security.User;



/**
 * 
 * @author sunil.kumar
 */
public class UserCsvUploaderUtil {
	
    public static User saveCSVFields(String [] userProperties) throws Exception { 
    	
    	User user=new User();   
		user.setUsername(userProperties[0]);	
    	String password = userProperties[1];   
		if(password != null && password.length()>=6) 
		   {
		    
			 user.setPassword(password);
		   }		
	 	
    	user.setFirstName(userProperties[2]);  
    	user.setMiddleName(userProperties[4]);	
		user.setLastName(userProperties[3]);			
		user.seteMail(userProperties[5]);			
		user.setCountry(userProperties[6]);		 
		user.setCity(userProperties[7]);	
		user.setTimeZone(userProperties[8]);
		user.setLanguage(userProperties[9]);
		user.setDescription(userProperties[10]);
		Role role = new Role();
		role.setRole(userProperties[11]);	
    	user.addRole(role);	
		
     	return user;
	

    }
}
