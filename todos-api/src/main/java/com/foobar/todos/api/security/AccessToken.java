/**
 * 
 */
package com.foobar.todos.api.security;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author pnayak
 * 
 *         This represents the access token granted upon successful authN
 * 
 */
@Document
public class AccessToken {

	@Id
	private String uuid;
	@DBRef
	private User user;
	private long grantTime;
	private long VALIDITY_PERIOD = 120 * 60 * 1000; // 2 hours

	public AccessToken() {
		super();
		grantTime = System.currentTimeMillis();
	}

	public AccessToken(String uuid) {
		super();
		this.uuid = uuid;
	}

	/**
	 * @return the grantTime
	 */
	public long getGrantTime() {
		return grantTime;
	}

	/**
	 * @param grantTime the grantTime to set
	 */
	public void setGrantTime(long grantTime) {
		this.grantTime = grantTime;
	}

	@JsonIgnore
	public boolean isValid() {

		long currentTime = System.currentTimeMillis();

		if (currentTime - grantTime > VALIDITY_PERIOD) {
			return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @param uuid
	 *            the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public boolean equals(Object obj) {
		AccessToken other = (AccessToken) obj;
		if (this.getUuid().equalsIgnoreCase(other.getUuid())) {
			return true;
		}
		return false;
	}
}
