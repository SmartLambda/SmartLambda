package edu.teco.smartlambda.container;

/**
 * This class holds a factory method for the {@link ContainerBuilder} used by the system
 */
public class BuilderFactory {
	
	/**
	 * This utility class shall not be instantiated
	 */
	private BuilderFactory() {
		// intentionally empty
	}
	
	/**
	 * @return the {@link ContainerBuilder} implementation that is used by the whole application
	 */
	public static ContainerBuilder getContainerBuilder() {
		return new DockerContainerBuilder();
	}
}
