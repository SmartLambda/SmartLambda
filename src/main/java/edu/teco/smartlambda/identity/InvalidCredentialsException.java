package edu.teco.smartlambda.identity;

/**
 * Created on 28.02.17.
 */
public class InvalidCredentialsException extends IdentityException{
	public InvalidCredentialsException() {
		super();
	}
	
	public InvalidCredentialsException(final String message) {
		super(message);
	}
	
	public InvalidCredentialsException(final Throwable cause) {
		super(cause);
	}
}
