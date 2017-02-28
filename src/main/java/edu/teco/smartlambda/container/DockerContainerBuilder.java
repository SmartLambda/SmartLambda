package edu.teco.smartlambda.container;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import edu.teco.smartlambda.configuration.ConfigurationService;
import edu.teco.smartlambda.shared.GlobalOptions;
import org.apache.commons.compress.utils.IOUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * A builder implementation for {@link DockerContainer}s
 */
public class DockerContainerBuilder implements ContainerBuilder {
	
	private final String containerId;
	private       String command;
	private final File   tmpDirectory;
	private       String template;
	
	public DockerContainerBuilder() {
		containerId = generateContainerId();
		
		tmpDirectory = new File(System.getProperty("java.io.tmpdir"), containerId);
		assert !tmpDirectory.exists() : "Temporary docker file directory already exists!";
		
		//noinspection ResultOfMethodCallIgnored
		tmpDirectory.mkdir();
	}
	
	@Override
	public DockerContainer build()
			throws DockerCertificateException, IOException, DockerException, InterruptedException, URISyntaxException {
		// copy execution service into tmp directory
		//// FIXME: 2/28/17
		final InputStream      inputStream  = DockerContainerBuilder.class.getClassLoader().getResourceAsStream("executionservice.jar");
		final FileOutputStream outputStream = new FileOutputStream(new File(tmpDirectory, "executionservice.jar"));
		outputStream.write(IOUtils.toByteArray(inputStream));
		outputStream.flush();
		outputStream.close();
		inputStream.close();
		
		final File       dockerFile = new File(tmpDirectory, "Dockerfile");
		final FileWriter writer     = new FileWriter(dockerFile);
		
		writer.write("FROM " + template + "\n");
		writer.write("COPY . ~\n");
		writer.write("WORKDIR ~\n");
		writer.write("EXPOSE " + GlobalOptions.PORT + "\n");
		writer.write("CMD " + this.command + "\n");
		writer.flush();
		writer.close();
		
		final DockerClient dockerClient = new DefaultDockerClient(
				ConfigurationService.getInstance().getConfiguration().getString("docker.socket", DockerContainer.DEFAULT_SOCKET));
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
	public ContainerBuilder setTemplate(final String template) {
		this.template = template;
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
