package com.foobar.todos.api;

/**
 * Represents a Task
 * 
 * @author pnayak
 *
 */
public class Task extends Entity {
	
	private String title;

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
}
