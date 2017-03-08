package edu.teco.smartlambda.lambda;

/**
 *
 */
public class DuplicateEventException extends RuntimeException {
	
	public DuplicateEventException(final String eventName) {
		super("Event with name \"" + eventName + "\" already exists.");
	}
}
