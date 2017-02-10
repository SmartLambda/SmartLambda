package edu.teco.smartlambda.execution;

import lombok.Data;

import java.io.Serializable;

/**
 * The serializable object that is returned by the {@link LambdaExecutionService} whenever a lambda is executed
 */
@Data
public class ExecutionReturnValue implements Serializable {
	private final String    returnValue;
	private final Throwable exception;
}
