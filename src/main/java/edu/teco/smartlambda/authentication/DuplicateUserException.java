package edu.teco.smartlambda.authentication;

/**
 * Created on 07.03.17.
 */
public class DuplicateUserException extends RuntimeException{
	public DuplicateUserException() {
		super();
	}
	
	public DuplicateUserException(final String message) {
		super(message);
	}
}
