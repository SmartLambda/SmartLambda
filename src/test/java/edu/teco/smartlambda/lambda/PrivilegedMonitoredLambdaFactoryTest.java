package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.runtime.RuntimeRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RuntimeRegistry.class)
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
		
		mockStatic(RuntimeRegistry.class);
		when(RuntimeRegistry.getInstance()).thenReturn(mock(RuntimeRegistry.class));
		
		// is it empty
		assertNull(emptyLambda.getOwner());
		assertNull(emptyLambda.getRuntime());
	}
}