package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created on 22.03.17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class GitHubIdentityProviderTest {
	
	private String answer;
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(User.class);
		PowerMockito.when(User.createUser(anyString())).thenAnswer(invocation -> {
			this.answer = (String) invocation.getArguments()[0];
			return null;
		});
	}
	
	@Test (expected = IdentitySyntaxException.class)
	public void testNullParameter() {
		new GitHubIdentityProvider().register(null);
	}
	
	@Test (expected = IdentitySyntaxException.class)
	public void testEmptyParameter() {
		new GitHubIdentityProvider().register(new HashMap<>());
	}
	
	@Test
	public void testGettersAndSetters() throws Exception{
		TestUtility.testGettersAndSetters(new GitHubIdentityProvider());
	}
	
	
}
