package edu.teco.smartlambda.container;

import edu.teco.smartlambda.container.docker.DockerContainer;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A container is a running instance of an {@link Image}.
 */
public interface Container {
	
	/**
	 * Get the standard input of a container. While this could technically also be part of {@link #attach(OutputStream, OutputStream)},
	 * {@link DockerContainer} requires it to be specially handled, because the Docker implementation lacks a method to attach any
	 * {@link InputStream}
	 *
	 * @return an input stream into the default input of the docker container
	 *
	 * @throws Exception on any container library specific exception
	 */
	public OutputStream getStdIn() throws Exception;
	
	/**
	 * Attach standard output and standard error stream of the container to two output streams where one can read of.
	 *
	 * @param stdOut Standard out
	 * @param stdErr Standard error
	 *
	 * @throws Exception on any container library specific exception
	 */
	public void attach(final OutputStream stdOut, final OutputStream stdErr) throws Exception;
	
	/**
	 * @return the container id, that the implementation can use to find a previously created container
	 */
	public String getId();
	
	/**
	 * @return the CPU time that has been consumed by this container during last execution
	 * @throws Exception on any container library specific exception
	 */
	public long getConsumedCPUTime() throws Exception;
}
