package edu.teco.smartlambda.configuration;

import edu.teco.smartlambda.BuildConfig;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * A service class loading and providing the {@link Configuration} instance that is used for configuring the system.
 */
public class ConfigurationService {
	private static ConfigurationService instance;
	private final  Configuration        configuration;
	
	/**
	 * Private constructor that builds the {@link Configuration} instance. This class is a singleton and shall only instantiate itself
	 */
	private ConfigurationService() {
		try {
			this.configuration = new Configurations().xml(BuildConfig.CONFIGURATION_PATH);
		} catch (final ConfigurationException e) {
			throw new ConfigurationLoaderException(e);
		}
	}
	
	/**
	 * @return the singleton instance of this service
	 */
	public static ConfigurationService getInstance() {
		if (instance == null) instance = new ConfigurationService();
		
		return instance;
	}
	
	/**
	 * @return the configuration instance of Apache
	 */
	public Configuration getConfiguration() {
		return this.configuration;
	}
}
