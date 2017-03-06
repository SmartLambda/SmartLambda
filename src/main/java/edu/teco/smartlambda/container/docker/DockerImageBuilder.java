package edu.teco.smartlambda.container.docker;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import edu.teco.smartlambda.configuration.ConfigurationService;
import edu.teco.smartlambda.container.ImageBuilder;
import lombok.Setter;
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
 * A builder implementation for {@link DockerImage}s
 */
public class DockerImageBuilder implements ImageBuilder {
	
	private final String containerId;
	private       String command;
	private final File   tmpDirectory;
	private       String template;
	
	@Setter
	private String runtimeLibrary;
	
	public DockerImageBuilder() {
		containerId = generateContainerId();
		
		tmpDirectory = new File(System.getProperty("java.io.tmpdir"), containerId);
		assert !tmpDirectory.exists() : "Temporary docker file directory already exists!";
		
		//noinspection ResultOfMethodCallIgnored
		tmpDirectory.mkdir();
	}
	
	@Override
	public DockerImage build()
			throws DockerCertificateException, IOException, DockerException, InterruptedException, URISyntaxException {
		final InputStream      inputStream  = DockerImageBuilder.class.getClassLoader().getResourceAsStream(this.runtimeLibrary);
		final FileOutputStream outputStream = new FileOutputStream(new File(tmpDirectory, this.runtimeLibrary));
		outputStream.write(IOUtils.toByteArray(inputStream));
		outputStream.flush();
		outputStream.close();
		inputStream.close();
		
		final File       dockerFile = new File(tmpDirectory, "Dockerfile");
		final FileWriter writer     = new FileWriter(dockerFile);
		
		writer.write("FROM " + template + "\n");
		writer.write("COPY . ~\n");
		writer.write("WORKDIR ~\n");
		writer.write("CMD " + this.command + "\n");
		writer.flush();
		writer.close();
		
		final DockerClient dockerClient = new DefaultDockerClient(
				ConfigurationService.getInstance().getConfiguration().getString("docker.socket", DockerClientProvider.DEFAULT_SOCKET));
		final String imageId = dockerClient.build(tmpDirectory.getAbsoluteFile().toPath(), DockerClient.BuildParam.name(containerId));
		
		//noinspection ResultOfMethodCallIgnored
		tmpDirectory.delete();
		
		return new DockerImage(imageId);
	}
	
	@Override
	public ImageBuilder setCommand(final String command) {
		this.command = command;
		return this;
	}
	
	@Override
	public ImageBuilder setTemplate(final String template) {
		this.template = template;
		return this;
	}
	
	@Override
	public ImageBuilder storeFile(final byte[] binary, final String name, final boolean executable) throws IOException {
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
		assert file.canExecute() == executable;
		
		return this;
	}
	
	/**
	 * @return a randomly generated ID for a container
	 */
	private String generateContainerId() {
		return UUID.randomUUID().toString();
	}
}
