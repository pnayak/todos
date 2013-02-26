/**
 * 
 */
package com.pragyasystems.knowledgeHub.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pnayak
 * 
 *         By default, this custom implementation copies over all non-null
 *         attributes from the provided entity into the existing entity and
 *         returns the existing entity for save/index update
 *         
 *         The only exception is the UUID - which is not copied over from 
 *         the provided entity into the new entity.
 * 
 *         Note that this implementation does not treat empty as a null. So, if
 *         the provided entity has an empty string, the existing attribute will
 *         end up pointing to an empty string
 * 
 *         Note also that this implementation does not treat an empty collection
 *         as a null... so if the provided entity has an empty collection and
 *         the existing entity has a non-empty collection, then it will be
 *         replaced with the empty collection - so beware of this
 * 
 */
public class CustomBeanUtilBean extends BeanUtilsBean {

	protected static final Logger LOG = LoggerFactory.getLogger(CustomBeanUtilBean.class);

	@Override
	public void copyProperties(Object existing, Object provided)
			throws IllegalAccessException, InvocationTargetException {
		
		try {
			LOG.debug("Copying " + PropertyUtils.describe(provided) + " into " + PropertyUtils.describe(existing));
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.copyProperties(existing, provided);
		
		try {
			LOG.debug("Updated entity is " + PropertyUtils.describe(existing));
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void copyProperty(Object dest, String name, Object value)
			throws IllegalAccessException, InvocationTargetException {
		if (name.equals("uuid")) {
			return; // Do not update the UUID
		}
		if (value == null || isEmptyCollection(value)) {
			return;
		}
		LOG.debug("Replacing " + name + " in " + dest + " with " + value);
		super.copyProperty(dest, name, value);
	}
	
	private boolean isEmptyCollection(Object object) {
		 if(object instanceof Collection){
			 return ((Collection)object).isEmpty();
		 }
		  return false ;
		}

}
