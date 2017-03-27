package edu.teco.smartlambda.container;

import java.io.IOException;

/**
 * A builder for images that shall be configured to execute the lambda on spawn
 */
public interface ImageBuilder {
	
	/**
	 * Builds a new {@link Image} instance with the previously configured settings
	 *
	 * @return a new concrete {@link Image} instance
	 *
	 * @throws Exception on any image engine specific error
	 */
	public Image build() throws Exception;
	
	/**
	 * Append a command to a image file that is executed upon image spawn
	 *
	 * @param command command to be executed
	 *
	 * @return this builder instance
	 */
	public ImageBuilder setCommand(final String command);
	
	/**
	 * Set the template the image derives from
	 *
	 * @param template docker image template
	 *
	 * @return this builder instance
	 */
	public ImageBuilder setTemplate(final String template);
	
	/**
	 * Stores a non-executable file in the image
	 *
	 * @param binary binary content
	 * @param name   file name
	 *
	 * @return this builder instance
	 */
	public default ImageBuilder storeFile(final byte[] binary, final String name) throws IOException {
		return this.storeFile(binary, name, false);
	}
	
	/**
	 * Stores a file in the image and sets the executable flag if required
	 *
	 * @param binary     binary content
	 * @param name       file name
	 * @param executable whether the file is executable
	 *
	 * @return this builder instance
	 */
	public ImageBuilder storeFile(final byte[] binary, final String name, final boolean executable) throws IOException;
}
