package edu.teco.smartlambda.lambda;

/**
 * Thrown when a lambda file is given to the system, that is not valid.
 */
public class InvalidLambdaException extends RuntimeException {
	public InvalidLambdaException(final String description) {
		super(description);
	}
}
