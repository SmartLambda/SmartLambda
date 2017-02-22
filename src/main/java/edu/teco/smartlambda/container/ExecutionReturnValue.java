package edu.teco.smartlambda.container;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Optional;

/**
 * The serializable object that is returned by the {@link Container} whenever a lambda is executed
 */
@RequiredArgsConstructor
public class ExecutionReturnValue implements Serializable {
	
	private final String    returnValue;
	private final Throwable exception;
	
	/**
	 * @return whether the execution resulted in an error
	 */
	public boolean isException() {
		return exception != null;
	}
	
	/**
	 * @return the serialized return value of the lambda function or an empty optional if the lambda execution failed
	 */
	public Optional<String> getReturnValue() {
		return Optional.ofNullable(this.returnValue);
	}
	
	/**
	 * @return the exception thrown while lambda execution or an empty optional if no exception was thrown
	 */
	public Optional<Throwable> getException() {
		return Optional.ofNullable(this.exception);
	}
}
