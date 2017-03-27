package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created on 21.03.17.
 */
public class GitHubCredentialTest {
	
	@Test
	public void coverdefaultConstructor() throws Exception {
		new GitHubCredential();
	}
	
	@Test
	public void testConstructor () {
		final String           token            = "GitHubCredentialTest.testConstructor";
		final User user = Mockito.mock(User.class);
		final GitHubCredential gitHubCredential = new GitHubCredential(token, user);
		Assert.assertEquals(gitHubCredential.getAccessToken(), token);
		Assert.assertEquals(gitHubCredential.getUser(), user);
	}
}