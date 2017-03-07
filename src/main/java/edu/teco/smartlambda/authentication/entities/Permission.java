package edu.teco.smartlambda.authentication.entities;

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
	
	/**
	 * Empty constructor, used by Hibernate
	 */
	public Permission() {
		
	}
	
	private Permission(final AbstractLambda lambda, final User user, final PermissionType type, final Key key) {
		this.lambda = LambdaDecorator.unwrap(lambda);
		this.user = user;
		this.permissionType = type;
		this.key = key;
	}
	
	/**
	 * Creates a new Permission object with the supplied parameters for the supplied key
	 * @param user the Permission will have a coverage for all lambdas of this user
	 * @param type the Type of the new Permission
	 * @param key the supplied key
	 */
	public Permission(final User user, final PermissionType type, final Key key) {
		this(null, user, type, key);
	}
	
	/**
	 * Creates a new Permission object with the supplied parameters for the supplied key
	 * @param lambda the Permission will be set for this lambda
	 * @param type the Type of the new Permission
	 * @param key the supplied key
	 */
	public Permission(final AbstractLambda lambda, final PermissionType type, final Key key) {
		this(lambda, null, type, key);
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
}
