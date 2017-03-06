package edu.teco.smartlambda.container;

import edu.teco.smartlambda.container.docker.DockerImageBuilder;

/**
 * This class holds a factory method for the {@link ImageBuilder} used by the system
 */
public class BuilderFactory {
	
	/**
	 * This utility class shall not be instantiated
	 */
	private BuilderFactory() {
		// intentionally empty
	}
	
	/**
	 * @return the {@link ImageBuilder} implementation that is used by the whole application
	 */
	public static ImageBuilder getContainerBuilder() {
		return new DockerImageBuilder();
	}
}
