package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.authentication.entities.User;

public class DuplicateLambdaException extends RuntimeException {
	private final User   owner;
	private final String name;
	
	public DuplicateLambdaException(final User owner, final String name) {
		super("Lambda with name \"" + name + "\" already exists");
		this.owner = owner;
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public User getOwner() {
		return owner;
	}
}
