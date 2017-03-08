package edu.teco.smartlambda.shared;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The serializable object that is returned by the Container whenever a lambda is executed
 */
public class ExecutionReturnValue implements Serializable {
	
	private final String returnValue;
	private final String exception;
	
	/**
	 * An execution return value or exception, that will be parsed into a string
	 *
	 * @param returnValue serialized return value
	 * @param exception   exception to be serialized
	 */
	public ExecutionReturnValue(final String returnValue, final Throwable exception) {
		this(returnValue, generateStackTrace(exception, false));
	}
	
	/**
	 * Generate stack traces from exceptions
	 *
	 * @param throwable an exception
	 *
	 * @return a stack trace string
	 */
	private static String generateStackTrace(final Throwable throwable, final boolean isCause) {
		if (throwable == null) return "";
		
		String msg = (isCause ? "Caused by: " : "") + throwable.getClass().getName() + ": " + throwable.getMessage() + "\n\t";
		msg += Arrays.stream(Arrays.copyOfRange(throwable.getStackTrace(), 0, throwable.getStackTrace().length - 5))
				.map(StackTraceElement::toString).collect(Collectors.joining("\n\t"));
		msg += (throwable.getCause() != throwable ? "\n" + generateStackTrace(throwable.getCause(), true) : "");
		
		return msg;
	}
	
	/**
	 * An Execution Return value or exception as string
	 *
	 * @param returnValue serialized return value
	 * @param exception   exception / error as string
	 */
	public ExecutionReturnValue(final String returnValue, final String exception) {
		this.returnValue = returnValue;
		this.exception = exception != null && exception.equals("") ? null : exception;
	}
	
	/**
	 * @return whether the execution resulted in an error
	 */
	public boolean isException() {
		return this.getException().isPresent();
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
	public Optional<String> getException() {
		return Optional.ofNullable(this.exception);
	}
}
