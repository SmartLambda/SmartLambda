package edu.teco.smartlambda.lambda;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class PrivilegedMonitoredLambdaFactoryTest {
	
	@Test
	public void getLambdaByOwnerAndName() throws Exception {
	
	}
	
	@Test
	public void createLambda() throws Exception {
		final AbstractLambda emptyLambda = new PrivilegedMonitoredLambdaFactory().createLambda();
		
		// not a prototype
		assertNotNull(emptyLambda);
		assertNotSame(LambdaFacade.getInstance().getFactory().createLambda(), emptyLambda);
		
		// is it empty
		assertNull(emptyLambda.getOwner());
		assertNull(emptyLambda.getRuntime());
	}
}