package edu.teco.smartlambda.rest.exception;

/**
 *
 */
public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(final String name) {
		super("User with name \"" + name + "\" not found");
	}
}
