/**
 * 
 */
package com.foobar.todos.api;

/**
 * COPYRIGHT (C) 2012 Pragya Systems. All Rights Reserved.
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         A simple About Representation object.
 * 
 *         Clients will use this as the entity that is referenced when invoking
 *         and obtaining results from calls to various Knowledge Service
 *         Resources
 * 
 *         All Representations go into the api package in the k-services-api
 *         sub-module
 * 
 *         This is in place mostly as a reference for developers just getting
 *         started and also to have a resource + representation that can be used
 *         to test that your service build is functional
 * 
 */
public class About {

	private String about;

	/**
	 * @param about
	 */
	public About(String about) {
		super();
		this.about = about;
	}

	public String getAbout() {
		return about;
	}
}
