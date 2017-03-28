package edu.teco.smartlambda.container.docker;

import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.Image;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class DockerImageBuilderTest {
	
	@Test
	public void simpleBuildTest() throws Exception {
		final Image image = new DockerImageBuilder().setCommand("exit").setTemplate("openjdk:8")
				.storeFile(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("jre8/executionservice.jar")),
						"executionservice.jar").build();
		final Container container;
		Assert.assertNotNull(container = image.start());
		DockerClientProvider.get().killContainer(container.getId());
		image.delete();
	}
}