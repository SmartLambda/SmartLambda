package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.lambda.Lambda;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by Matteo on 01.02.2017.
 */
public class Permission {
	
	private int id;
	private User user;
	private Lambda lambda;
	private PermissionType permissionType;
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int getId() {
		return id;
	}
	
	private void setId(final int id) {
		this.id = id;
	}
	
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "User")
	public User getUser() {
		return user;
	}
	
	private void setUser(final User user) {
		this.user = user;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Lambda")
	public Lambda getLambda() {
		return lambda;
	}
	
	private void setLambda(final Lambda lambda) {
		this.lambda = lambda;
	}
	
	@Column(name = "PermissionType")
	public PermissionType getPermissionType() {
		return permissionType;
	}
	
	private void setPermissionType(PermissionType permissionType) {
		this.permissionType = permissionType;
	}
	
	public Permission(Lambda lambda, PermissionType type) {
		this.setLambda(lambda);
		this.setPermissionType(type);
	}
	
	public  Permission (User user, PermissionType type) {
		this.setUser(user);
		this.setPermissionType(type);
	}
	
}
