package edu.teco.smartlambda.authentication;

/**
 * Created on 15.02.17.
 */
public class DuplicateKeyException extends RuntimeException{
	public DuplicateKeyException() {
		super();
	}
	public DuplicateKeyException(final String message) {
		super(message);
	}
}
