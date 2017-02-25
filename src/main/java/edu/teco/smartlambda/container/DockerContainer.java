package edu.teco.smartlambda.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import edu.teco.smartlambda.configuration.ConfigurationService;

/**
 *
 */
public class DockerContainer implements Container {
	
	public static final String DEFAULT_SOCKET = "unix:///var/run/docker.sock";
	
	final String dockerContainerId;
	
	public DockerContainer(final String containerId) {
		this.dockerContainerId = containerId;
	}
	
	@Override
	public void start() throws DockerException, InterruptedException {
		new DefaultDockerClient(ConfigurationService.getInstance().getConfiguration().getString("docker.socket", DEFAULT_SOCKET))
				.startContainer(this.dockerContainerId);
	}
	
	@Override
	public String getContainerId() {
		return this.dockerContainerId;
	}
}
