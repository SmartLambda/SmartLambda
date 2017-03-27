package edu.teco.smartlambda.container.docker;

import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.Image;
import edu.teco.smartlambda.shared.GlobalOptions;
import edu.teco.smartlambda.utility.TestUtility;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assume.assumeNotNull;

/**
 *
 */
public class HttpHijackingWorkaroundTest {
	
	@Test
	public void testConstruct() throws Exception {
		TestUtility.coverPrivateDefaultConstructor(HttpHijackingWorkaround.class);
	}
	
	@Test
	public void getOutputStream() throws Exception {
		final DockerImageBuilder imageBuilder = new DockerImageBuilder();
		try (InputStream binStream = HttpHijackingWorkaroundTest.class.getClassLoader().getResourceAsStream("lambda.jar")) {
			assumeNotNull(binStream);
			
			imageBuilder.storeFile(IOUtils.toByteArray(binStream), GlobalOptions.JRE_8_BINARY_NAME);
		}
		
		final Image image = imageBuilder.storeFile(
				IOUtils.toByteArray(HttpHijackingWorkaround.class.getClassLoader().getResourceAsStream("executionservice" + ".jar")),
				"executionservice.jar").setCommand("java -jar executionservice.jar").setTemplate("openjdk:8").build();
		
		final Container container = image.start();
		assertNotNull(container.getStdIn());
		
		// stop the container execution
		DockerClientProvider.get().stopContainer(container.getId(), 1);
		image.delete();
	}
}