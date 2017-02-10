package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.lambda.Lambda;

import java.util.List;

/**
 * Created by Matteo on 01.02.2017.
 */
public class Key {
	
	private String name;
	private String id;
	private User user;
	
	public Key(String id, User user) {
		
	}
	
	public List<Permission> getPermissions() {
		
		/*
			TODO: access the database
		 */
		return null;
	}
	
	public boolean hasPermission(Lambda lambda, PermissionType type) {
		
		for (Permission perm : this.getPermissions()) {
			if (perm.getLambda().equals(lambda) && perm.getPermissionType().equals(type)) {
					return true;
			}
		}
		return false;
	}
	
	public boolean hasPermission(User user, PermissionType type) {
		
		for (Permission perm : this.getPermissions()) {
			if (perm.getUser().equals(user) && perm.getPermissionType().equals(type)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPrimaryKey() {	return false;	}
	
	public void delete() {
		
		/*
			TODO: delete from the database
		 */
	}
	
	public void grantPermission(Lambda lambda, PermissionType type) {
		
		Permission perm = new Permission(type, lambda);
		/*
			TODO:  Add it to the database
		 */
	}
	
	public void grantPermission(User user, PermissionType type) {
		
	}
	
	public void revokePermission(Lambda lambda, PermissionType type) {
	}
	
	public void revokePermission(User user, PermissionType type) {
		
	}
	
}
