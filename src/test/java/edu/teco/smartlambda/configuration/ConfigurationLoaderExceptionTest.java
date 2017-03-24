package edu.teco.smartlambda.configuration;

import edu.teco.smartlambda.utility.TestUtility;
import org.junit.Test;

/**
 *
 */
public class ConfigurationLoaderExceptionTest {
	@Test
	public void coverException() throws Exception {
		TestUtility.coverException(ConfigurationLoaderException.class);
	}
}