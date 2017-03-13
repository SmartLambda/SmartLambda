package edu.teco.smartlambda.container.docker;

import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.Image;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import edu.teco.smartlambda.shared.GlobalOptions;
import edu.teco.smartlambda.utility.TestUtility;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.InputStream;

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
		// this test cannot be mocked, since it is a very deep reflection into the subjacent libraries
		// therefore a real image must be created
		assumeNotNull(DockerClientProvider.get());
		
		final DockerImageBuilder imageBuilder = new DockerImageBuilder();
		try (InputStream binStream = HttpHijackingWorkaroundTest.class.getClassLoader().getResourceAsStream("lambda.jar")) {
			assert binStream != null : "The test case lambda executable is missing.";
			
			imageBuilder.storeFile(IOUtils.toByteArray(binStream), GlobalOptions.JRE_8_BINARY_NAME);
		}
		
		RuntimeRegistry.getInstance().getRuntimeByName("jre8").setupContainerImage(imageBuilder);
		final Image image = imageBuilder.build();
		
		final Container container = image.start();
		assumeNotNull(container.getStdIn());
		
		// stop the container execution
		DockerClientProvider.get().stopContainer(container.getId(), 1);
		image.delete();
	}
}