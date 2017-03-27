package edu.teco.smartlambda.lambda;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class PrivilegedMonitoredLambdaFactoryTest {
	
	private PrivilegedMonitoredLambdaFactory factory;
	
	@Before
	public void setup() {
		this.factory = new PrivilegedMonitoredLambdaFactory();
	}
	
	@Test
	public void createLambda() throws Exception {
		final AbstractLambda emptyLambda = this.factory.createLambda();
		
		// not a prototype
		assertNotNull(emptyLambda);
		assertNotSame(LambdaFacade.getInstance().getFactory().createLambda(), emptyLambda);
		
		// is it empty
		assertNull(emptyLambda.getOwner());
		assertNull(emptyLambda.getRuntime());
	}
}