package edu.teco.smartlambda.container;

/**
 * A factory that creates the containers that are used by the system. This is the central place to change the globally used container
 * implementation
 */
public final class ContainerFactory {
	
	/**
	 * This is a static utility class that shall not be instantiated
	 */
	private ContainerFactory() {
		// intentionally empty
	}
	
	/**
	 * @return a new empty container istance
	 */
	public static Container createContainer() {
		return new DockerContainer();
	}
	
	/**
	 * Get an existing container
	 *
	 * @param containerId the unique identifier that represents a previously created and saved container
	 *
	 * @return a container instance representing a previously created container
	 */
	public static Container getContainerById(final String containerId) {
		return new DockerContainer(containerId);
	}
}
