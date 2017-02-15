package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.lambda.Lambda;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by Matteo on 01.02.2017.
 */
public class Permission {
	
	private int id;
	private Key key;
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
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Key", nullable = false)
	public Key getKey() {
		return key;
	}
	
	private void setKey(final Key key) {
		this.key = key;
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
	
	public Permission(Lambda lambda, PermissionType type, Key key) {
		this.setLambda(lambda);
		this.setPermissionType(type);
		this.setKey(key);
	}
	
	public  Permission (User user, PermissionType type, Key key) {
		this.setUser(user);
		this.setPermissionType(type);
		this.setKey(key);
	}
	
}
