package edu.teco.smartlambda.container.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import edu.teco.smartlambda.container.Container;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

import static edu.teco.smartlambda.container.docker.DockerClientProvider.DEFAULT_SOCKET;

public class DockerContainer implements Container {
	private final String id;
	private LogStream stream = null;
	
	DockerContainer(final String id) {
		this.id = id;
	}
	
	private void ensureAttached() throws DockerException, InterruptedException {
		if (this.stream != null) return;
		
		this.stream = DockerClientProvider.get()
				.attachContainer(this.getId(), DockerClient.AttachParameter.STREAM, DockerClient.AttachParameter.STDIN,
						DockerClient.AttachParameter.STDOUT, DockerClient.AttachParameter.STDERR);
	}
	
	@Override
	public WritableByteChannel getStdIn() throws Exception {
		this.ensureAttached();
		return HttpHijackingWorkaround.getOutputStream(this.stream, DEFAULT_SOCKET);
	}
	
	@Override
	public void attach(final OutputStream stdOut, final OutputStream stdErr) throws Exception {
		this.ensureAttached();
		this.stream.attach(stdOut, stdErr, true);
	}
	
	@Override
	public String getId() {
		return this.id;
	}
}
