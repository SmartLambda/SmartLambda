package edu.teco.smartlambda.rest.exception;

public class RuntimeNotFoundException extends InvalidLambdaDefinitionException {
	public RuntimeNotFoundException(final String name) {
		super("Runtime with name \"" + name + "\" not found");
	}
}
