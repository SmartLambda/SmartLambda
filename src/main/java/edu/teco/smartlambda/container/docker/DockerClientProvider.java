package edu.teco.smartlambda.container.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import edu.teco.smartlambda.configuration.ConfigurationService;

class DockerClientProvider {
	static final String DEFAULT_SOCKET = "unix:///var/run/docker.sock";
	
	private static final ThreadLocal<DockerClient> dockerClient = ThreadLocal.withInitial(() -> new DefaultDockerClient(
			ConfigurationService.getInstance().getConfiguration().getString("docker.socket", DEFAULT_SOCKET)));
	
	public static DockerClient get() {
		return dockerClient.get();
	}
}
