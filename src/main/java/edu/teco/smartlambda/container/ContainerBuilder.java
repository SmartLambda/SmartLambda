package edu.teco.smartlambda.container;

import java.io.IOException;

/**
 * A builder for container files that shall be configured to execute the lambda on spawn
 */
public interface ContainerBuilder {
	
	/**
	 * Builds a new {@link Container} instance with the previously configured settings
	 *
	 * @return a new concrete Container instance
	 *
	 * @throws Exception on any container engine specific error
	 */
	public Container build() throws Exception;
	
	/**
	 * Append a command to a container file that is executed upon container spawn
	 *
	 * @param command command to be executed
	 *
	 * @return this builder instance
	 */
	public ContainerBuilder setCommand(final String command);
	
	/**
	 * Set the template the container derives from
	 *
	 * @param template docker image template
	 *
	 * @return this builder instance
	 */
	public ContainerBuilder setTemplate(final String template);
	
	/**
	 * Stores a non-executable file in the container
	 *
	 * @param binary binary content
	 * @param name   file name
	 *
	 * @return this builder instance
	 */
	public default ContainerBuilder storeFile(final byte[] binary, final String name) throws IOException {
		return this.storeFile(binary, name, false);
	}
	
	/**
	 * Stores a file in the container and sets the executable flag if required
	 *
	 * @param binary     binary content
	 * @param name       file name
	 * @param executable whether the file is executable
	 *
	 * @return this builder instance
	 */
	public ContainerBuilder storeFile(final byte[] binary, final String name, final boolean executable) throws IOException;
	
	public void setRuntimeLibrary(final String runtimeLibrary);
}
