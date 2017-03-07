package edu.teco.smartlambda.lambda;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeNotNull;

/**
 *
 */
public class LambdaFactoryTest {
	
	@Test
	public void getLambdaByOwnerAndName() throws Exception {
		// TODO
	}
	
	@Test
	public void createLambda() throws Exception {
		assumeNotNull(LambdaFacade.getInstance());
		assumeNotNull(LambdaFacade.getInstance().getFactory());
		
		final AbstractLambda emptyLambda = LambdaFacade.getInstance().getFactory().createLambda();
		
		// not a prototype
		assertNotNull(emptyLambda);
		assertNotSame(LambdaFacade.getInstance().getFactory().createLambda(), emptyLambda);
		
		// is it empty
		assertNull(emptyLambda.getOwner());
		assertNull(emptyLambda.getRuntime());
	}
}