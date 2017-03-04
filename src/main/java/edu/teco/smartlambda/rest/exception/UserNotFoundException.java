package edu.teco.smartlambda.rest.exception;

/**
 * Created on 04.03.17.
 */
public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(final String name) {
		super("User with name \"" + name + "\" not found");
	}
}
