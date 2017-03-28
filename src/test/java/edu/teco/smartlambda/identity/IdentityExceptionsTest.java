package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;

/**
 * Created on 21.03.17.
 */
public class IdentityExceptionsTest {
	
	@Test
	public void identityExceptionTest() throws Exception {
		TestUtility.coverException(IdentityException.class);
	}
	
	@Test
	public void identitySyntaxExceptionTest() throws Exception {
		TestUtility.coverException(IdentitySyntaxException.class);
	}
	
	@Test
	public void invalidCredentialsExceptionTest() throws Exception {
		TestUtility.coverException(InvalidCredentialsException.class);
	}
	
	@Test
	public void gitHubCredentialDuplicateExceptionTest() throws Exception {
		TestUtility.coverException(GitHubCredentialDuplicateException.class);
	}
	
}