package com.foobar.todos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.foobar.todos.api.security.User;
import com.foobar.todos.db.security.UserRepository;
import com.foobar.todos.resources.security.UserUtils;
import com.yammer.dropwizard.lifecycle.Managed;

/**
 * 
 * @author Prashant Nayak
 * 
 *         Create superAdmin user on startup (first time)
 */
public class SetupService implements Managed {

	private static final Logger LOG = LoggerFactory
			.getLogger(SetupService.class);

	@Autowired
	private UserRepository userRepository;

	public SetupService() {

	}

	public SetupService(UserRepository userRepository) {
		this.userRepository = userRepository;

	}

	@Override
	public void start() throws Exception {
		doSetup();
	}

	@Override
	public void stop() throws Exception {
		// Do nothing ....
	}

	private void doSetup() throws Exception {

		LOG.info("******* Creating Super Admin User *******");
		User superAdmin = new User();

		superAdmin.setUsername("superAdmin");
		superAdmin.setPassword(UserUtils.hashPassword("foobar"));

		superAdmin = userRepository.save(superAdmin);

	}

}