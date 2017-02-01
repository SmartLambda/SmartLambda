package edu.teco.smartlambda.configuration;

import org.apache.commons.configuration2.Configuration;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class ConfigurationServiceTest {
	@Test
	public void getInstance() {
		assertNotNull(ConfigurationService.getInstance());
		assertSame(ConfigurationService.getInstance(), ConfigurationService.getInstance());
	}
	
	@Test
	public void getConfiguration() {
		Configuration configuration = ConfigurationService.getInstance().getConfiguration();
		
		assertNotNull(configuration);
	}
}