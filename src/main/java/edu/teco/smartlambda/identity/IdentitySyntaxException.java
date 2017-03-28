package edu.teco.smartlambda.identity;

/**
 *
 */
public class IdentitySyntaxException extends IdentityException{
	public  IdentitySyntaxException() {
		super();
	}
	
	public IdentitySyntaxException(final String messsage) {
		super(messsage);
	}
	
	public IdentitySyntaxException(final Throwable cause) {
		super(cause);
	}
}
