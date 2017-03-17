package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;

/**
 *
 */
public class DuplicateEventExceptionTest {
	
	@Test
	public void coverException() throws Exception {
		TestUtility.coverException(DuplicateEventException.class);
	}
}