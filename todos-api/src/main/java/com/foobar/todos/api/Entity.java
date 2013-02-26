/**
 * 
 */
package com.foobar.todos.api;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 */
@Document
public class Entity {

	@Id
	@Indexed
	private String uuid;
	private String version;
	private long lastUpdatedTime;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * The creation time for this entity in milliseconds
	 * 
	 * @return
	 */
	public long getCreationTime() {
		if (uuid != null || ObjectId.isValid(uuid)) {
			ObjectId mongoId = new ObjectId(uuid);
			return mongoId.getTime();
		} else {
			return 0l;
		}
	}
	
	/**
	 * @return the lastUpdatedTime
	 */
	public long getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	/**
	 * @param currentTimeMillis
	 */
	public void setLastUpdatedTime(long lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	@Override
	public String toString() {
		return "Entity [uuid = " + getUuid() + ", version = " + getVersion()
				+ "]";
	}

	@Override
	public boolean equals(Object obj) {
		Entity other = (Entity) obj;
		if (this.getUuid().equalsIgnoreCase(other.getUuid())) {
			return true;
		}
		return false;
	}
}
