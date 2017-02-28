package edu.teco.smartlambda.identity;

/**
 * Created on 28.02.17.
 */
public class IdentityProviderNotFoundException extends IdentityException{
	public IdentityProviderNotFoundException() {
		super();
	}
	public IdentityProviderNotFoundException(String message) {
		super(message);
	}
}
