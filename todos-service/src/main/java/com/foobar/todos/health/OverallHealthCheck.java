/**
 * 
 */
package com.foobar.todos.health;

import org.springframework.stereotype.Component;

import com.yammer.metrics.core.HealthCheck;

/**
 * 
 * @author Prashant Nayak (pnayak)
 * 
 *         An overall health check for the service.
 * 
 *         TODO: Need to supplement this to provide an actual check at runtime
 * 
 */
@Component
public class OverallHealthCheck extends HealthCheck {

	/**
	 * 
	 */
	public OverallHealthCheck() {
		super("ToDo's Service Health Check ");
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yammer.metrics.core.HealthCheck#check()
	 */
	@Override
	protected Result check() throws Exception {
		// TODO Auto-generated method stub
		return Result.healthy(getName() + "Healthy :-)");
	}
}
