package edu.teco.smartlambda.container;

import edu.teco.smartlambda.container.docker.DockerImage;

/**
 * A factory that creates the images that are used by the system. This is the central place to change the globally used container
 * implementation
 */
public final class ImageFactory {
	
	/**
	 * This is a static utility class that shall not be instantiated
	 */
	private ImageFactory() {
		// intentionally empty
	}
	
	/**
	 * Gets an existing image
	 *
	 * @param imageId the unique identifier that represents a previously created and saved image
	 *
	 * @return an {@link Image} instance representing a previously created image
	 */
	public static Image getImageById(final String imageId) {
		return new DockerImage(imageId);
	}
}
