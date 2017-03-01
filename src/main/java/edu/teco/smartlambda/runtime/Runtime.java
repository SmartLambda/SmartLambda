package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.container.ContainerBuilder;

/**
 * The runtime environment of a lambda
 */
public interface Runtime {
	
	/**
	 * Setup the runtime specific configuration of a container
	 *
	 * @param builder the builder instance that builds the new container
	 */
	public void setupContainerImage(final ContainerBuilder builder);
	
	/**
	 * @return the name of the runtime
	 */
	public String getName();
	
	/**
	 * @return the name of the lambda binary file inside the container. It is the same for all lambdas that are of the same runtime
	 */
	public String getBinaryName();
}
