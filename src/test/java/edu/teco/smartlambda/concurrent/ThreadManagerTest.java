package edu.teco.smartlambda.concurrent;

import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * Test Case for {@link ThreadManager}
 */
public class ThreadManagerTest {
	
	@Test
	public void construct() throws Exception {
		TestUtility.coverPrivateDefaultConstructor(ThreadManager.class);
	}
	
	@Test
	public void testGetExecutorService() {
		// not null
		assertNotSame(null, ThreadManager.getExecutorService());
		
		// single instance
		assertSame(ThreadManager.getExecutorService(), ThreadManager.getExecutorService());
	}
}