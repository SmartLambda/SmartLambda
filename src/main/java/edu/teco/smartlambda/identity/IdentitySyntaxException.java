package edu.teco.smartlambda.identity;

/**
 * Created on 28.02.17.
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
