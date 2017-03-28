package edu.teco.smartlambda.configuration;

import org.apache.commons.configuration2.ex.ConfigurationException;
import spark.Spark;

/**
 * A wrapper for any {@link ConfigurationException} that extends {@link RuntimeException}, so the exception can be thrown unchecked to
 * the {@link Spark} filters.
 */
public class ConfigurationLoaderException extends RuntimeException {
	
	/**
	 * @param cause an exception that was thrown inside the apache configuration service
	 */
	ConfigurationLoaderException(final ConfigurationException cause) {
		super(cause);
	}
}
