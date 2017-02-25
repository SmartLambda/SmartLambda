package edu.teco.smartlambda.container;

import org.junit.Test;

/**
 *
 */
public class DockerContainerBuilderTest {
	
	@Test
	public void simpleBuildTest() throws Exception {
		Container container = new DockerContainerBuilder().setCommand("example command").build();
		container.delete();
	}
}