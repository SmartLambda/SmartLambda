package edu.teco.smartlambda.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import edu.teco.smartlambda.configuration.ConfigurationService;
import edu.teco.smartlambda.lambda.Lambda;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * A builder implementation for {@link DockerContainer}s
 */
public class DockerContainerBuilder implements ContainerBuilder {
	
	private final String containerId;
	private       String command;
	private final File   tmpDirectory;
	
	public DockerContainerBuilder() {
		containerId = generateContainerId();
		
		tmpDirectory = new File(System.getProperty("java.io.tmpdir"), containerId);
		assert !tmpDirectory.exists() : "Temporary docker file directory already exists!";
		
		//noinspection ResultOfMethodCallIgnored
		tmpDirectory.mkdir();
	}
	
	@Override
	public DockerContainer build() throws DockerCertificateException, IOException, DockerException, InterruptedException {
		final File       dockerFile = new File(tmpDirectory, "Dockerfile");
		final FileWriter writer     = new FileWriter(dockerFile);
		
		//// FIXME: 2/20/17
		writer.write("FROM openjdk:8\n");
		writer.write("COPY . ~\n");
		writer.write("WORKDIR ~\n");
		writer.write("EXPOSE " + Lambda.PORT + "\n");
		writer.write("CMD " + this.command + "\n");
		writer.flush();
		writer.close();
		
		//// FIXME: 2/22/17
		final DockerClient dockerClient = new DefaultDockerClient(
				ConfigurationService.getInstance().getConfiguration().getString("docker.socket", DockerContainer.DEFAULT_SOCKET));
		System.out.println(tmpDirectory.getAbsoluteFile().toPath());
		final String imageId = dockerClient.build(tmpDirectory.getAbsoluteFile().toPath(), DockerClient.BuildParam.name(containerId));
		
		//noinspection ResultOfMethodCallIgnored
		tmpDirectory.delete();
		
		return new DockerContainer(imageId);
	}
	
	@Override
	public ContainerBuilder setCommand(final String command) {
		this.command = command;
		return this;
	}
	
	@Override
	public ContainerBuilder storeFile(final byte[] binary, final String name, final boolean executable) throws IOException {
		final File file = new File(tmpDirectory, name);
		assert !file.exists();
		
		//noinspection ResultOfMethodCallIgnored
		file.createNewFile();
		
		final DataOutputStream output = new DataOutputStream(new FileOutputStream(file));
		output.write(binary);
		output.flush();
		output.close();
		
		//noinspection ResultOfMethodCallIgnored
		file.setExecutable(executable);
		assert file.canExecute();
		
		return this;
	}
	
	/**
	 * @return a randomly generated ID for a container
	 */
	private String generateContainerId() {
		return UUID.randomUUID().toString();
	}
}
