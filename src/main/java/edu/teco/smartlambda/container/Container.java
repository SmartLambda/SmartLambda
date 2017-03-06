package edu.teco.smartlambda.container;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

/**
 * A container is a running instance of an {@link Image}.
 */
public interface Container {
	public WritableByteChannel getStdIn() throws Exception;
	
	public void attach(final OutputStream stdOut, final OutputStream stdErr) throws Exception;
	
	public String getId();
}
