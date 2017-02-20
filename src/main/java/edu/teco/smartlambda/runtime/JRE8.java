package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.container.ContainerBuilder;

/**
 * A {@link Runtime} implementation for Java 8
 */
public class JRE8 implements Runtime {
	
	private static final String NAME        = "JRE8";
	public static final  String BINARY_NAME = "lambda.jar";
	
	@Override
	public void setupContainerImage(final ContainerBuilder builder) {
		
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
