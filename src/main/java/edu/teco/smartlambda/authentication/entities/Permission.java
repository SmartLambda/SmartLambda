package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.lambda.LambdaDecorator;
import lombok.Getter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static javax.persistence.GenerationType.IDENTITY;

/**
 *
 */
@Entity
@Table(name = "Permission")
public class Permission {
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	@Getter
	private int id;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user")
	private User user = null;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "key")
	private Key key;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lambda")
	private Lambda lambda = null;
	private PermissionType permissionType;
	
	public Permission() {
		
	}
	
	public Permission(final AbstractLambda lambda, final User user, final PermissionType type, final Key key) {
		this.lambda = LambdaDecorator.unwrap(lambda);
		this.user = user;
		this.permissionType = type;
		this.key = key;
		//persist();
	}
	
	public Permission(final User user, final PermissionType type, final Key key) {
		this(null, user, type, key);
	}
	
	public Permission(final AbstractLambda lambda, final PermissionType type, final Key key) {
		this(lambda, null, type, key);
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
	
	private void setLambda(final AbstractLambda lambda) {
		this.lambda = LambdaDecorator.unwrap(lambda);
	}
	
	/**
	 * Returns the PermissionType of this Permission
	 *
	 * @return permissionType
	 */
	@Column(name = "PermissionType")
	public PermissionType getPermissionType() {
		return this.permissionType;
	}
	
	private void setPermissionType(final PermissionType permissionType) {
		this.permissionType = permissionType;
	}
	
	private void persist() {
		Application.getInstance().getSessionFactory().getCurrentSession().save(this);
	}
}
