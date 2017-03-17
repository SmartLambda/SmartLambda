package edu.teco.smartlambda.authentication;

/**
 * Is thrown when the authenticated Key has no permission for the actual operation.
 */
public class InsufficientPermissionsException extends RuntimeException{
	public InsufficientPermissionsException() {
		super();
	}
}
