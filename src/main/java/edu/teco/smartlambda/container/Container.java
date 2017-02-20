package edu.teco.smartlambda.container;

/**
 * A container is the virtualization layer for lambda execution.
 */
public interface Container {
	
	/**
	 * Starts a container of an image.
	 *
	 * @throws Exception on any virtualization engine specific exception
	 */
	public void start() throws Exception;
	
	/**
	 * @return a unique id to identify the container later
	 */
	public String save();
}
