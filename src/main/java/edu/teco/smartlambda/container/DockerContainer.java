package edu.teco.smartlambda.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import edu.teco.smartlambda.configuration.ConfigurationService;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;

/**
 *
 */
public class DockerContainer implements Container {
	
	public static final String                    DEFAULT_SOCKET = "unix:///var/run/docker.sock";
	private static      ThreadLocal<DockerClient> dockerClient   = ThreadLocal.withInitial(() -> new DefaultDockerClient(
			ConfigurationService.getInstance().getConfiguration().getString("docker.socket", DEFAULT_SOCKET)));
	
	private final String    dockerImageId;
	private       LogStream stream;
	
	public DockerContainer(final String containerId) {
		this.dockerImageId = containerId;
	}
	
	@Override
	public WritableByteChannel start() throws Exception {
		final DockerClient      client    = dockerClient.get();
		final ContainerCreation container =
				client.createContainer(ContainerConfig.builder().image(this.dockerImageId).attachStdin(true).openStdin(true).build());
		client.startContainer(container.id());
		
		stream = client.attachContainer(container.id(), DockerClient.AttachParameter.STREAM, DockerClient.AttachParameter.STDIN,
				DockerClient.AttachParameter.STDOUT, DockerClient.AttachParameter.STDERR);
		
		return HttpHijackingWorkaround.getOutputStream(stream, DEFAULT_SOCKET);
	}
	
	@Override
	public void attach(final OutputStream stdOut, final OutputStream stdErr) throws Exception {
		stream.attach(stdOut, stdErr, true);
	}
	
	@Override
	public String getOutput() {
		return stream.readFully();
	}
	
	@Override
	public String getContainerId() {
		return this.dockerImageId;
	}
	
	@Override
	public void delete() throws DockerException, InterruptedException {
		dockerClient.get().removeImage(dockerImageId, true, false);
	}
}
