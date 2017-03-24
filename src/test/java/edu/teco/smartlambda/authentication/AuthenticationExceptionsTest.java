package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;

/**
 * Created on 13.03.17.
 */
public class AuthenticationExceptionsTest {
	
	@Test
	public void duplicateKeyExceptionTest() throws Exception{
		TestUtility.coverException(DuplicateKeyException.class);
	}
	
	@Test
	public void duplicateUserExceptionTest() throws Exception{
		TestUtility.coverException(DuplicateUserException.class);
	}
	
	@Test
	public void insufficientPermissionsException() throws Exception{
		TestUtility.coverException(InsufficientPermissionsException.class);
	}
	
	@Test
	public void notAuthenticatedException() throws Exception{
		TestUtility.coverException(NotAuthenticatedException.class);
	}
}
