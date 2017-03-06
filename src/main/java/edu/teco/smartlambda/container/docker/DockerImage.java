package edu.teco.smartlambda.container.docker;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.Image;

/**
 *
 */
public class DockerImage implements Image {
	private final String    dockerImageId;
	
	public DockerImage(final String imageId) {
		this.dockerImageId = imageId;
	}
	
	@Override
	public Container start() throws Exception {
		final DockerClient client = DockerClientProvider.get();
		final ContainerCreation container =
				client.createContainer(ContainerConfig.builder().image(this.dockerImageId).attachStdin(true).openStdin(true).build());
		client.startContainer(container.id());
		
		return new DockerContainer(container.id());
	}
	
	@Override
	public String getId() {
		return this.dockerImageId;
	}
	
	@Override
	public void delete() throws DockerException, InterruptedException {
		DockerClientProvider.get().removeImage(this.dockerImageId, true, false);
	}
}
