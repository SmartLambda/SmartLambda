package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.container.ImageBuilder;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * A {@link Runtime} implementation for Java 8
 */
public class JRE8 implements Runtime {
	
	private static final String NAME        = "jre8";
	private static final String BINARY_NAME = "lambda.jar";
	
	private static final String EXECUTION_SERVICE_NAME = "executionservice.jar";
	
	@Override
	public void setupContainerImage(final ImageBuilder builder) {
		try {
			builder.storeFile(IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("executionservice.jar")),
					"executionservice.jar");
		} catch (final IOException e) {
			throw new RuntimeException("Failed to read execution service JAR resource", e);
		}
		
		builder.setCommand("java -jar " + EXECUTION_SERVICE_NAME).setTemplate("openjdk:8-jre-alpine");
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public String getBinaryName() {
		return BINARY_NAME;
	}
}
