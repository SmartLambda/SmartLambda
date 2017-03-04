package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 28.02.17.
 */
public class NullIdentityProviderTest {
	@Test
	public void testReturnValue() {
		IdentityProvider ip = IdentityProviderRegistry.getInstance().getIdentityProviderByName("NullIdentityProvider");
		Map<String, String> params = new HashMap<>();
		String input = "testname123";
		params.put("name", input);
		User   user   = ip.register(params);
		Assert.assertEquals(input, user.getName());
	}
}