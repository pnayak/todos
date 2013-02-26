/**
 * 
 */
package com.pragyasystems.knowledgeHub.resources.user;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pragyasystems.knowledgeHub.api.security.User;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 */
public class UserUtils {

	private static final Logger LOG = LoggerFactory.getLogger(UserUtils.class);
	public static final String PREFIX = "{bcrypt}";

	/**
	 * @param cleartext
	 * @return
	 * 
	 *         Hash a clear text password
	 * @throws Exception
	 */
	public static String hashPassword(String cleartext) throws Exception {

		if (isHashed(cleartext)) {
			throw new Exception("Provided password is already hashed!!");
		}

		// gensalt's log_rounds parameter determines the complexity
		// the work factor is 2**log_rounds, and the default is 10
		String hashed = PREFIX + BCrypt.hashpw(cleartext, BCrypt.gensalt());

		return hashed;
	}

	/**
	 * @param hashed
	 * @return
	 * 
	 *         Check that an unencrypted password matches one that has
	 *         previously been hashed
	 * @throws Exception
	 */
	public static boolean passwordValid(String cleartext, String hashed)
			throws Exception {

		if (isHashed(cleartext)) {
			throw new Exception("Provided password is already hashed!!");
		}
		
		// We need to remove the PREFIX before calling BCrypt.checkpw() since
		// it expects the hashed password to start with salt version "$2a$"
		hashed = hashed.substring(8);

		if (BCrypt.checkpw(cleartext, hashed)) {
			LOG.debug("Password matches");
			return true;
		} else {
			LOG.debug("Password does not match");
			return false;
		}
	}

	public static boolean isHashed(String candidate) {

		if (candidate.startsWith(PREFIX)) {
			return true;
		}

		return false;
	}

	/**
	 * @param providedUser
	 * @param storedUser
	 * @throws Exception
	 */
	public static void validatePasswordUpdate(User providedUser, User storedUser)
			throws Exception {
		if (providedUser.getPassword() != null) {
			if (!UserUtils.isHashed(providedUser.getPassword())) {
				// hash the updated user password. Will throw exception if
				// provided password is already hashed
				// i.e. if the UI passes in the old password
				try {
					providedUser.setPassword(UserUtils
							.hashPassword(providedUser.getPassword()));
				} catch (Exception e) {
					throw new WebApplicationException(Status.BAD_REQUEST);
				}
			} else {
				if (providedUser.getPassword().compareTo(storedUser.getPassword()) != 0) {
					LOG.error("Tried to update old password with invalide (Hashed) password");
					throw new Exception(
							"Tried to update old password with invalide (Hashed) password");
				}
			}
		}
	}

}
