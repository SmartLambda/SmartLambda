package edu.teco.smartlambda.rest.exception;

public class LambdaNotFoundException extends RuntimeException {
	public LambdaNotFoundException(final String name) {
		super("Lambda with name \"" + name + "\" not found");
	}
}
