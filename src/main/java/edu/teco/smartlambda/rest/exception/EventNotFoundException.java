package edu.teco.smartlambda.rest.exception;

/**
 *
 */
public class EventNotFoundException extends RuntimeException {
	public EventNotFoundException(final String name) {
		super("Event with name \"" + name + "\" not found");
	}
}
