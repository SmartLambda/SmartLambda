package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created on 28.02.17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(User.class)
public class NullIdentityProviderTest {
	
	private String answer;
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(User.class);
		PowerMockito.when(User.createUser(anyString())).thenAnswer(invocation -> {
			this.answer = (String) invocation.getArguments()[0];
			return null;
		});
	}
	
	@Test
	public void testReturnValue() {
		final IdentityProvider    ip     = new NullIdentityProvider();
		final Map<String, String> params = new HashMap<>();
		final String              input  = "testname123";
		params.put("name", input);
		ip.register(params);
		Assert.assertEquals(input, this.answer);
	}
	
	@Test (expected = IdentitySyntaxException.class)
	public void testNullParameter() {
		new NullIdentityProvider().register(null);
	}
	
	@Test (expected = IdentitySyntaxException.class)
	public void testEmptyParameter() {
		new NullIdentityProvider().register(new HashMap<>());
	}
}