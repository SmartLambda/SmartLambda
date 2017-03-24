package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.configuration.ConfigurationService;
import org.apache.commons.configuration2.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationService.class})
public class RuntimeRegistryTest {
	
	@Before
	public void setup() {
		mockStatic(ConfigurationService.class);
		
		final ConfigurationService mockedService = mock(ConfigurationService.class);
		final Configuration        mockedConfig  = mock(Configuration.class);
		
		when(ConfigurationService.getInstance()).thenReturn(mockedService);
		when(mockedService.getConfiguration()).thenReturn(mockedConfig);
		when(mockedConfig.getList(String.class, "runtimes.runtime")).thenReturn(Collections.singletonList(JRE8.class.getName()));
	}
	
	@Test
	public void getInstance() throws Exception {
		assertNotNull(RuntimeRegistry.getInstance());
		assertEquals(RuntimeRegistry.getInstance(), RuntimeRegistry.getInstance());
	}
	
	@Test
	public void getRuntimeByName() throws Exception {
		assertEquals(RuntimeRegistry.getInstance().getRuntimeByName("jre8").getClass(), JRE8.class);
	}
}