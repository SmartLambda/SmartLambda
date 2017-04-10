package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.container.ImageBuilder;

/**
 * The runtime environment of a lambda
 */
public interface Runtime {
	
	/**
	 * Setup the runtime specific configuration of a container
	 *
	 * @param builder the builder instance that builds the new container
	 */
	public void setupContainerImage(final ImageBuilder builder);
	
	/**
	 * @return the name of the runtime
	 */
	public String getName();
	
	/**
	 * @return the name of the lambda binary file inside the container. It is the same for all lambdas that are of the same runtime
	 */
	public String getBinaryName();
	
	/**
	 * Verify that a binary file, given as bytes, is a valid lambda executable
	 *
	 * @param binaryData the lambda binary content
	 *
	 * @return true, if the binary is valid, false if it was not a lambda executable
	 */
	public boolean verifyBinary(final byte[] binaryData);
}
