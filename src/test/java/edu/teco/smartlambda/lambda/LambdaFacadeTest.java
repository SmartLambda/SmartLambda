package edu.teco.smartlambda.lambda;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * Test case for {@link LambdaFacade}
 */
public class LambdaFacadeTest {
	
	/**
	 * Test the singleton aspect
	 */
	@Test
	public void getInstance() throws Exception {
		assertNotNull(LambdaFacade.getInstance());
		assertSame(LambdaFacade.getInstance(), LambdaFacade.getInstance());
	}
	
	@Test
	public void getFactory() throws Exception {
		assertNotNull(LambdaFacade.getInstance().getFactory());
	}
}