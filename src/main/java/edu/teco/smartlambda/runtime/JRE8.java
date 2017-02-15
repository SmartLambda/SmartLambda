package edu.teco.smartlambda.runtime;

/**
 * A {@link Runtime} implementation for Java 8
 */
public class JRE8 implements Runtime {
	
	private static final String NAME = "JRE8";
	
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
		//// FIXME: 2/15/17
		return "something like LambdaExecutionService.jar";
	}
}
