package edu.teco.smartlambda.rest.exception;

abstract public class InvalidLambdaDefinitionException extends RuntimeException {
	InvalidLambdaDefinitionException(final String message) {
		super(message);
	}
}
