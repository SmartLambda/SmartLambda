package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 *
 */
@SuppressWarnings("ThrowableNotThrown")
public class DuplicateLambdaExceptionTest {
	
	public void coverException() throws Exception {
		TestUtility.coverException(DuplicateLambdaException.class);
	}
	
	@Test
	public void getName() throws Exception {
		final User dummy = Mockito.mock(User.class);
		
		final DuplicateLambdaException exception = new DuplicateLambdaException(dummy, "dummy");
		assertEquals("dummy", exception.getName());
	}
	
	@Test
	public void getOwner() throws Exception {
		final User dummy = Mockito.mock(User.class);
		
		final DuplicateLambdaException exception = new DuplicateLambdaException(dummy, "dummy");
		assertEquals(dummy, exception.getOwner());
	}
}