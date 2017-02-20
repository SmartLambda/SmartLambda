package edu.teco.smartlambda.runtime;

/**
 * A {@link Runtime} implementation for Java 8
 */
public class JRE8 implements Runtime {
	
	private static final String NAME        = "JRE8";
	public static final  String BINARY_NAME = "lambda.jar";
	
	@Override
	public String getCommand() {
		//// FIXME: 2/15/17
		return null;
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
