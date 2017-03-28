package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.configuration.ConfigurationService;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConfigurationService.class)
public class IdentityProviderRegistryTest {
	
	@Before
	public void setup() {
		mockStatic(ConfigurationService.class);
		Mockito.when(ConfigurationService.getInstance()).thenReturn(mock(ConfigurationService.class));
		
		Mockito.when(ConfigurationService.getInstance().getConfiguration()).thenReturn(mock(Configuration.class));
		Mockito.when(ConfigurationService.getInstance().getConfiguration().getList(eq(String.class), anyString()))
				.thenReturn(Collections.singletonList(GitHubIdentityProvider.class.getName()));
	}
	
	@Test
	public void getInstanceReturnsSingleton() throws Exception {
		assertNotNull(IdentityProviderRegistry.getInstance());
		assertSame(IdentityProviderRegistry.getInstance(), IdentityProviderRegistry.getInstance());
	}
	
	@Test
	public void getIdentityProviderByName() throws Exception {
		assertTrue(IdentityProviderRegistry.getInstance().getIdentityProviderByName("github").isPresent());
		assertEquals(IdentityProviderRegistry.getInstance().getIdentityProviderByName("github").get().getClass(),
				GitHubIdentityProvider.class);
	}
}