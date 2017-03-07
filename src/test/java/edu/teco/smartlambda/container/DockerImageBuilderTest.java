package edu.teco.smartlambda.container;

import edu.teco.smartlambda.container.docker.DockerImageBuilder;
import org.junit.Test;

/**
 *
 */
public class DockerImageBuilderTest {
	
	@Test
	public void simpleBuildTest() throws Exception {
		final Image image =
				new DockerImageBuilder().setCommand("example command").setTemplate("openjdk:8").setRuntimeLibrary("executionservice.jar")
						.build();
		assert image.start() != null;
		image.delete();
	}
}