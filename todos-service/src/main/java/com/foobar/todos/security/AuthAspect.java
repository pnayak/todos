/**
 * 
 */
package com.foobar.todos.security;

//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Prashant Nayak (pnayak)
 * 
 */
//@Component
//@Aspect
public class AuthAspect {

	private static final Logger LOG = LoggerFactory.getLogger(AuthAspect.class);

	//@Before("execution(* com.foobar.todos.resources.AboutResource.tellAbout(..))")
	public void doAccessCheck() {
		LOG.info("AuthAspect: was invoked");
	}

}
