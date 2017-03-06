package edu.teco.smartlambda.container;

/**
 * Images are used to create containers for lambda execution.
 */
public interface Image {
	
	/**
	 * Starts a container of an image.
	 *
	 * @throws Exception on any virtualization engine specific exception
	 */
	public Container start() throws Exception;
	
	/**
	 * @return a unique Id that may be used to reference the image
	 */
	public String getId();
	
	public void delete() throws Exception;
}
