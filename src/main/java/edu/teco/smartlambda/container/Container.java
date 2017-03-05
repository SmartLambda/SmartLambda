package edu.teco.smartlambda.container;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

/**
 * A container is the virtualization layer for lambda execution.
 */
public interface Container {
	
	/**
	 * Starts a container of an image.
	 *
	 * @throws Exception on any virtualization engine specific exception
	 */
	public WritableByteChannel start() throws Exception;
	
	public void attach(final OutputStream stdOut, final OutputStream stdErr) throws Exception;
	
	public String getOutput();
	
	/**
	 * @return a unique id that can be used to reference the container
	 */
	public String getContainerId();
	
	public void delete() throws Exception;
}
