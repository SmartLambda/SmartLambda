package edu.teco.smartlambda.container.docker;

import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.Image;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class DockerImageBuilderTest {
	
	@Test
	public void simpleBuildTest() throws Exception {
		final Image image =
				new DockerImageBuilder().setCommand("exit").setTemplate("openjdk:8").setRuntimeLibrary("executionservice.jar").build();
		final Container container;
		Assert.assertNotNull(container = image.start());
		DockerClientProvider.get().killContainer(container.getId());
		image.delete();
	}
}