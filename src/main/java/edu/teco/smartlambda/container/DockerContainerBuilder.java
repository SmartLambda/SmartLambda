package edu.teco.smartlambda.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

/**
 * A builder implementation for {@link DockerContainer}s
 */
public class DockerContainerBuilder implements ContainerBuilder {
	
	private String command;
	
	@Override
	public DockerContainer build() throws DockerCertificateException, IOException, DockerException, InterruptedException {
		final String containerId = generateContainerId();
		
		final File tmpDirectory = new File(containerId);
		assert !tmpDirectory.exists() : "Temporary docker file directory already exists!";
		//noinspection ResultOfMethodCallIgnored
		tmpDirectory.mkdir();
		
		final File       dockerFile = new File(tmpDirectory, "Dockerfile");
		final FileWriter writer     = new FileWriter(dockerFile);
		
		//// FIXME: 2/20/17
		writer.write("EXPOSE 31337");
		writer.write("CMD " + this.command);
		writer.flush();
		writer.close();
		
		final DockerClient dockerClient = DefaultDockerClient.fromEnv().build();
		final String       imageId      = dockerClient.build(tmpDirectory.toPath(), DockerClient.BuildParam.name(containerId));
		
		//noinspection ResultOfMethodCallIgnored
		tmpDirectory.delete();
		
		// TODO debug code
		if (!Objects.equals(imageId, containerId)) throw new AssertionError("image id != container id");
		
		return new DockerContainer(containerId);
	}
	
	@Override
	public ContainerBuilder setCommand(final String command) {
		this.command = command;
		return this;
	}
	
	@Override
	public ContainerBuilder storeFile(final byte[] binary, final String name, final boolean executable) {
		//// FIXME: 2/20/17
		return this;
	}
	
	/**
	 * @return a randomly generated ID for a container
	 */
	private String generateContainerId() {
		return UUID.randomUUID().toString();
	}
}
