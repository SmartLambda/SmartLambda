package edu.teco.smartlambda.identity;

/**
 * Created on 28.02.17.
 */
public class IdentityException extends RuntimeException{
	public IdentityException() {
		super();
	}
	
	public IdentityException(final String message) {
		super(message);
	}
	
	public IdentityException(final Throwable cause) {
		super(cause);
	}
}
