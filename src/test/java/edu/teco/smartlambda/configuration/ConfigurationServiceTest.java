package edu.teco.smartlambda.configuration;

import edu.teco.smartlambda.BuildConfig;
import org.apache.commons.configuration2.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;

public class ConfigurationServiceTest {
	@BeforeClass
	public static void verifyConfigurationExists() {
		final File configurationPath = new File(BuildConfig.CONFIGURATION_PATH);
		assumeTrue("Configuration file exists", configurationPath.exists() && configurationPath.canRead() && !configurationPath.isDirectory());
	}
	
	@Test
	public void getInstance() {
		assertNotNull(ConfigurationService.getInstance());
		assertSame(ConfigurationService.getInstance(), ConfigurationService.getInstance());
	}
	
	@Test
	public void getConfiguration() {
		final Configuration configuration = ConfigurationService.getInstance().getConfiguration();
		
		assertNotNull(configuration);
	}
}