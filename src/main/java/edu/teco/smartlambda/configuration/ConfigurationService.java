package edu.teco.smartlambda.configuration;

import edu.teco.smartlambda.BuildConfig;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfigurationService {
	private static ConfigurationService instance;
	private final  Configuration        configuration;
	
	private ConfigurationService() {
		try {
			this.configuration = new Configurations().xml(BuildConfig.CONFIGURATION_PATH);
		} catch (final ConfigurationException e) {
			throw new ConfigurationLoaderException(e);
		}
	}
	
	public static ConfigurationService getInstance() {
		if (instance == null) instance = new ConfigurationService();
		
		return instance;
	}
	
	public Configuration getConfiguration() {
		return this.configuration;
	}
}
