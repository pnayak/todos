/**
 * 
 */
package com.foobar.todos.api.security;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.foobar.todos.api.Entity;

/**
 * @author pnayak
 * 
 * Represents a user in the system
 * 
 */
@Document
public class User extends Entity {

	@Id
	private String uuid;
	private String username;
	private String password;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return this.password;
	}

}
