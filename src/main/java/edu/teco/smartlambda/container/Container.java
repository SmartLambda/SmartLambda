package edu.teco.smartlambda.container;

import java.io.OutputStream;

/**
 * A container is a running instance of an {@link Image}.
 */
public interface Container {
	
	/**
	 * @return
	 *
	 * @throws Exception
	 */
	public OutputStream getStdIn() throws Exception;
	
	public void attach(final OutputStream stdOut, final OutputStream stdErr) throws Exception;
	
	public String getId();
	
	public long getConsumedCPUTime() throws Exception;
}
