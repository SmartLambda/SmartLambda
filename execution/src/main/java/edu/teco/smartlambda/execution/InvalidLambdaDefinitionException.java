package edu.teco.smartlambda.execution;

/**
 * Exception, that is thrown when a lambda cannot be executed because it is illegally implemented or has got an invalid meta data file
 */
public class InvalidLambdaDefinitionException extends Exception {
	
	/**
	 * Create an InvalidLambdaDefinitionException with a description of the error
	 *
	 * @param description further description
	 */
	InvalidLambdaDefinitionException(final String description) {
		super(description);
	}
}
