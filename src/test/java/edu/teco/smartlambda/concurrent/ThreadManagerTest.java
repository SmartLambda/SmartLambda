package edu.teco.smartlambda.concurrent;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * Test Case for {@link ThreadManager}
 */
public class ThreadManagerTest {
	
	@Test
	public void testGetExecutorService() {
		// not null
		assertNotSame(null, ThreadManager.getExecutorService());
		
		// single instance
		assertSame(ThreadManager.getExecutorService(), ThreadManager.getExecutorService());
	}
}