package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.lambda.Lambda;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by Matteo on 01.02.2017.
 */
@Entity
@Table(name = "Permission")
public class Permission {
	
	private int id;
	private User user = null;
	private Lambda lambda = null;
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
	
	/**
	 * Returns the User Object if this is a Permission for al Lambda, null otherwise
	 * @return the User Object
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "User")
	public User getUser() {
		return user;
	}
	
	private void setUser(final User user) {
		this.user = user;
	}
	
	/**
	 * Returns the Lambda Object if this is a Permission for al Lambda, null otherwise
	 * @return the Lambda Object
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Lambda")
	public Lambda getLambda() {
		return lambda;
	}
	
	private void setLambda(final Lambda lambda) {
		this.lambda = lambda;
	}
	
	/**
	 * Returns the PermissionType of this Permission
	 * @return permissionType
	 */
	@Column(name = "PermissionType")
	public PermissionType getPermissionType() {
		return permissionType;
	}
	
	private void setPermissionType(PermissionType permissionType) {
		this.permissionType = permissionType;
	}
	
	/**
	 * Creates a Permission for the supplied Lambda and PermissionType
	 * @param lambda
	 * @param type
	 */
	public Permission(Lambda lambda, PermissionType type) {
		this.setLambda(lambda);
		this.setPermissionType(type);
	}
	
	/**
	 * Creates a Permission for the supplied User and PermissionType
	 * @param user
	 * @param type
	 */
	public  Permission (User user, PermissionType type) {
		this.setUser(user);
		this.setPermissionType(type);
	}
	
}
