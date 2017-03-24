package edu.teco.smartlambda.identity;

import org.apache.commons.configuration2.ImmutableConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Created on 22.03.17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ImmutableConfiguration.class)
public class IdentityProviderRegistryTest {
	
	@Test
	public void getInstanceNotNull() throws Exception {
		Assert.assertNotNull(IdentityProviderRegistry.getInstance());
	}
	
	@Test
	public void getInstanceReturnsSingleton() throws Exception {
		final IdentityProviderRegistry irFirst = IdentityProviderRegistry.getInstance();
		assert irFirst!=null;
		final IdentityProviderRegistry irSecond = IdentityProviderRegistry.getInstance();
		assert irSecond!=null;
		Assert.assertSame(irFirst, irSecond);
	}

}