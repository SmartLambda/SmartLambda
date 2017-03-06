package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.container.ContainerBuilder;

/**
 * A {@link Runtime} implementation for Java 8
 */
public class JRE8 implements Runtime {
	
	private static final String NAME        = "jre8";
	private static final String BINARY_NAME = "lambda.jar";
	
	private static final String EXECUTION_SERVICE_NAME = "executionservice.jar";
	
	@Override
	public void setupContainerImage(final ContainerBuilder builder) {
		builder.setRuntimeLibrary(EXECUTION_SERVICE_NAME);
		builder.setCommand("java -jar " + EXECUTION_SERVICE_NAME).setTemplate("openjdk:8");
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
