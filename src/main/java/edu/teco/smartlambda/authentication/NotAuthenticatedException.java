package edu.teco.smartlambda.authentication;

/**
 * Is thrown when the actual operation needs a permission but there is no authenticated Key
 */
public class NotAuthenticatedException extends RuntimeException {
	public NotAuthenticatedException() {
		super("Invalid credentials provided");
	}
}
