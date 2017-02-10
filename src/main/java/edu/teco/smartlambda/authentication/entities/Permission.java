package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.lambda.Lambda;

/**
 * Created by Matteo on 01.02.2017.
 */
public class Permission {
	
	
	private Key key;
	private User user;
	private Lambda lambda;
	private PermissionType permissionType;
	
	public Permission(PermissionType type, Lambda lambda) {
		
	}
	
	public  Permission (PermissionType type, User user) {
		
	}
	
	public Key getKey() { return key; }
	
	public User getUser() {	return user; }
	
	public Lambda getLambda() { return lambda; }
	
	public PermissionType getPermissionType() { return permissionType; }
	
	
}
