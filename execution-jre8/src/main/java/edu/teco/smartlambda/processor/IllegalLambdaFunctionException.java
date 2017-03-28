package edu.teco.smartlambda.processor;

import edu.teco.smartlambda.execution.LambdaFunction;

/**
 * An exception thrown by the {@link LambdaFunctionProcessor} when the user annotated a function as {@link LambdaFunction} that does not
 * meet the specifications
 */
public class IllegalLambdaFunctionException extends RuntimeException {
	
	/**
	 * @param description further description of the error
	 */
	public IllegalLambdaFunctionException(final String description) {
		super(description);
	}
}
