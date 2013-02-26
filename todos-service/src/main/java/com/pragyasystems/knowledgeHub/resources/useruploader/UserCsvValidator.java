package com.pragyasystems.knowledgeHub.resources.useruploader;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 
 * @author sunil.kumar
 */
public class UserCsvValidator {	
	
	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public static String getError(String[] userProp, int lineNo) {
		
		if (userProp.length < 8 || userProp.length > 12){
			return "Line No. " + lineNo + " : Incorrect No. of fields in CSV line"; 
		} 
		if (userProp[0].equals("") || userProp[0] == null ||userProp[2].equals("") || userProp[2] == null|| userProp[3].equals("") || userProp[3] == null
				|| userProp[5].equals("") || userProp[5] == null	|| userProp[7].equals("") || userProp[7] == null	|| userProp[8].equals("") || userProp[8] == null || userProp[11].equals("")|| userProp[11] == null	
		) {
			return "Line No. " + lineNo + " : UserName or FirstName or LastName or email or city or country or role can not be blank"; 
		}
		
		if (userProp[1].equals("") && userProp[1].length()<6 || userProp[1] == null) {
			return "Line No. " + lineNo + " : Enter at least six character";
		}
		
		if (!isValidEmail(userProp[5])) {
			return "Line No. " + lineNo + " : Incorrect Email in CSV line";
		}		
		return null;
	  }
	   private static boolean isValidEmail(String email) {
		Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
	}

}
