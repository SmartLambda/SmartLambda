package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.lambda.Lambda;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.Set;

/**
 * Created by Matteo on 01.02.2017.
 */
public class Key {
	
	private String id;
	private String name;
	private User   user;
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	public String getId() {
		return id;
	}
	
	public void setId(final String id) {
		this.id = id;
	}
	
	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "User", nullable = false)
	private User getUser() {
		return user;
	}
	
	private void setUser(User user) {
		this.user = user;
	}
	
	private Set<Permission> getPermissions() {
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
