package edu.teco.smartlambda.identity;

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
		Map<String, String> params = new HashMap<String, String>();
		String input = "testname123";
		params.put("name", input);
		ip.register(params);
		assert ip.getName().isPresent();
		String result = ip.getName().get();
		Assert.assertEquals(input, result);
	}
}