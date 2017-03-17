package edu.teco.smartlambda.rest.exception;

public class MissingSourceException extends InvalidLambdaDefinitionException {
	public MissingSourceException() {
		super("Missing source code or binary container");
	}
}
