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
	public String start() throws Exception;
	
	/**
	 * @return a unique id that can be used to reference the container
	 */
	public String getContainerId();
	
	public void delete() throws Exception;
}
