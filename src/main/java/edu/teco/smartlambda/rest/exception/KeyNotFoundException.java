package edu.teco.smartlambda.rest.exception;

public class KeyNotFoundException extends RuntimeException {
	public KeyNotFoundException(final String name) {
		super("Key with name \"" + name + "\" not found");
	}
}
