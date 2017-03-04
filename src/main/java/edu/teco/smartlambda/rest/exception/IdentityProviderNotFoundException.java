package edu.teco.smartlambda.rest.exception;

public class IdentityProviderNotFoundException extends RuntimeException {
	public IdentityProviderNotFoundException(final String name) {
		super("Identitiy provider with name \"" + name + "\" not found");
	}
}
