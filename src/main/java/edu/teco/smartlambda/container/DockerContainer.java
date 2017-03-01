package edu.teco.smartlambda.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import edu.teco.smartlambda.configuration.ConfigurationService;

/**
 *
 */
public class DockerContainer implements Container {
	
	public static final String DEFAULT_SOCKET = "unix:///var/run/docker.sock";
	
	final String dockerImageId;
	
	public DockerContainer(final String containerId) {
		this.dockerImageId = containerId;
	}
	
	@Override
	public String start() throws DockerException, InterruptedException {
		final DockerClient client =
				new DefaultDockerClient(ConfigurationService.getInstance().getConfiguration().getString("docker.socket", DEFAULT_SOCKET));
		final ContainerCreation container = client.createContainer(ContainerConfig.builder().image(this.dockerImageId).build());
		client.startContainer(container.id());
		return client.inspectContainer(container.id()).networkSettings().ipAddress();
	}
	
	@Override
	public String getContainerId() {
		return this.dockerImageId;
	}
}
