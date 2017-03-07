package edu.teco.smartlambda;

import edu.teco.smartlambda.configuration.ConfigurationService;
import org.junit.Ignore;
import org.junit.Test;
import spark.Spark;

import static org.junit.Assert.assertEquals;

public class ApplicationTest {
	@Test
	@Ignore
	public void testValidPortSelected() {
		Integer port = ConfigurationService.getInstance().getConfiguration().getInteger("rest.port", null);
		
		assertEquals(port != null ? port : 80, Spark.port());
	}
}