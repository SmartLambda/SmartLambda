package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class MonitoringServiceTest {
	
	private AbstractLambda lambda;
	
	@Before
	public void setUp() {
		lambda = Mockito.mock(Lambda.class);
	}
	
	@Test
	public void onLambdaExecutionEndTest() {
	}
}