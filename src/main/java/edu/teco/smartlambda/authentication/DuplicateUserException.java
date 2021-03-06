package edu.teco.smartlambda.authentication;

/**
 * Is thrown when a name conflicts occurs because a User with the same name already exists
 */
public class DuplicateUserException extends RuntimeException{
	public DuplicateUserException() {
		super();
	}
	
	public DuplicateUserException(final String message) {
		super(message);
	}
}
