package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.lambda.Lambda;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by Matteo on 01.02.2017.
 */
@Entity
@Table(name = "Permission")
public class Permission {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	@Getter
	private int            id;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private User           user;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private Key            key;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	private Lambda         lambda;
	private PermissionType permissionType;
	
	public Permission() {
	}
	
	private void setId(final int id) {
		this.id = id;
	}
	
	private void setUser(final User user) {
		this.user = user;
	}
	
	private void setKey(final Key key) {
		this.key = key;
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
	
	public Permission(User user, PermissionType type) {
		this.setUser(user);
		this.setPermissionType(type);
	}
}
