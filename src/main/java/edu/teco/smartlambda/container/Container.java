package edu.teco.smartlambda.container;

/**
 * A container is the virtualization layer for lambda execution.
 */
public interface Container {
	
	/**
	 * Starts a container of an image.
	 *
	 * @return the wrapped return value or exception of the lambda execution
	 *
	 * @throws Exception on any virtualization engine specific exception
	 */
	public ExecutionReturnValue start() throws Exception;
}
