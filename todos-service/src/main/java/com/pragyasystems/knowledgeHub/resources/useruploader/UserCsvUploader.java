package com.pragyasystems.knowledgeHub.resources.useruploader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.google.common.collect.Lists;
import com.pragyasystems.knowledgeHub.api.security.User;

/**
 * This will upload CSV file to database.
 * 
 * @author sunil.kumar
 * 
 */

public class UserCsvUploader {

	private InputStream inputStream;
	private int rowsInserted = 0;

	/**
	 * 
	 * @param inputStream
	 *            CSV file inputstream
	 * @param consumerService
	 *            Used to store CSV file data into DB
	 */
	public UserCsvUploader(InputStream inputStream ) {
		this.inputStream = inputStream;
	
	}

	/**
	 * This method will upload CSV file to DB. Erroneous line in CSV file will
	 * be ignored silently.
	 * 
	 * @return {@link List} of errors in CSVfile
	 */
	public Object[] upload() {
		List<String> errors = Lists.newArrayList();
		List<User> userList = Lists.newArrayList();
		try {
			// Create new Reader
			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream));
			String line = null;
			int lineNo = 1;
			// Get CSV file line
			while ((line = br.readLine()) != null && line.trim() != "") {
				if (lineNo != 1) {
					String[] userProp = line.split(",");
					for (int i = 0; i < userProp.length; i++) {
						userProp[i] = userProp[i].trim();
					}
					// Pass this line to Validator
					String validatorRes = UserCsvValidator.getError(userProp,
							lineNo);
					// If Response code is error code, ignore this line and go
					// to next line
					if (validatorRes != null) {
						errors.add(validatorRes);
					} else {
						User user = UserCsvUploaderUtil.saveCSVFields(userProp);
						if (user != null) {
							rowsInserted++;
							userList.add(user);
						}
					}
				}
				lineNo++;
			}
		} catch (Exception e) {
			errors.add(e.getMessage());
		}
		return new Object[] { errors, userList };
	}

	public int getRowsInserted() {
		return rowsInserted;
	}

}
