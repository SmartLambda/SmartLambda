package edu.teco.smartlambda.container;

import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class BuilderFactoryTest {
	
	@Test
	public void construct() throws Exception {
		TestUtility.coverPrivateDefaultConstructor(BuilderFactory.class);
	}
	
	@Test
	public void getContainerBuilder() throws Exception {
		assertNotNull(BuilderFactory.getContainerBuilder());
	}
}