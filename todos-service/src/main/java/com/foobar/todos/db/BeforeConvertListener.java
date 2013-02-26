/**
 * 
 */
package com.foobar.todos.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.stereotype.Component;

import com.foobar.todos.api.Entity;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 */
@Component
public class BeforeConvertListener extends AbstractMongoEventListener<Entity> {

	protected static final Logger LOG = LoggerFactory
			.getLogger(BeforeConvertListener.class);

	@Override
	public void onBeforeConvert(Entity entity) {
		LOG.debug("Setting lastUpdatedTime on entity " + entity + " of type "
				+ entity.getClass().getCanonicalName());
		entity.setLastUpdatedTime(System.currentTimeMillis());
	}
}