package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.lambda.Lambda;
import lombok.Getter;
import org.hibernate.Session;

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
	@JoinColumn(name="user")
	private User           user = null;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="key")
	private Key            key;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="lambda")
	private Lambda         lambda = null;
	private PermissionType permissionType;
	
	public Permission() {
		
	}
	
	/**
	 * Creates a Permission for the supplied Lambda and PermissionType
	 * @param lambda
	 * @param type
	 */
	public Permission(Lambda lambda, PermissionType type) {
		
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		this.lambda = lambda;
		this.permissionType = type;
		session.save(this);
		session.getTransaction().commit();
	}
	
	
	/**
	 * Creates a Permission for the supplied User and PermissionType
	 * @param user
	 * @param type
	 */
	public  Permission (User user, PermissionType type) {
		
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		this.user = user;
		this.permissionType = type;
		session.save(this);
		session.getTransaction().commit();
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
	
	
	
}
