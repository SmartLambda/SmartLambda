package edu.teco.smartlambda.container.docker;

import edu.teco.smartlambda.concurrent.ThreadManager;
import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 *
 */
public class DockerClientProviderTest {
	
	@Test
	public void construct() throws Exception {
		TestUtility.coverPrivateDefaultConstructor(DockerClientProvider.class);
	}
	
	@Test
	public void get() throws Exception {
		assertNotNull(DockerClientProvider.get());
		assertSame(DockerClientProvider.get(), DockerClientProvider.get());
		assertNotSame(DockerClientProvider.get(), ThreadManager.getExecutorService().submit(DockerClientProvider::get).get());
	}
}