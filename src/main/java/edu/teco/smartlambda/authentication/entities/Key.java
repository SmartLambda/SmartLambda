package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.lambda.Lambda;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Matteo on 01.02.2017.
 */
@Entity
@Table(name = "Key")
public class Key {
	
	private String          id;
	private String          name;
	private User            user;
	private Set<Permission> permissions;
	
	
	public Key(String id, String name, User user) {
		
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		this.id = id;
		this.name = name;
		this.user = user;
		this.permissions = new HashSet<>();
		
		session.save(this);
		session.getTransaction().commit();
	}
	
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	private String getId() {
		return id;
	}
	
	private void setId(final String id) {
		this.id = id;
	}
	
	
	/**
	 * Returns the Keys Name
	 * @return name
	 */
	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}
	
	private void setName(final String name) {
		this.name = name;
	}
	
	
	/**
	 * Returns the Keys User
	 * @return user
	 */
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "User", nullable = false)
	public User getUser() {
		return user;
	}
	
	private void setUser(User user) {
		this.user = user;
	}
	
	
	/**
	 * Returns the Keys Permissions as a Set
	 * @return permissions
	 */
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "Key")
	public Set<Permission> getPermissions() {
		return permissions;
	}
	
	private void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}
	
	
	/**
	 * Checks if the Key has the Permission of the supplied PermissionType for the supplied Lambda
	 * @param lambda a Permission has to be a Permission for this Lambda
	 * @param type a Permission has to be a Permission of this Type
	 * @return returns if the queried Permission exists
	 */
	public boolean hasPermission(Lambda lambda, PermissionType type) {
		if (lambda == null) throw new IllegalStateException("Input parameter lambda == null");
		for (Permission perm : permissions) {
			if (perm.getLambda().equals(lambda) && perm.getPermissionType().equals(type)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Checks if the Key has the Permission of the supplied PermissionType for the supplied User
	 * @param user a Permission has to be a Permission for this User
	 * @param type a Permission has to be a Permission of this Type
	 * @return returns if the queried Permission exists
	 */
	public boolean hasPermission(User user, PermissionType type) {
		if (user == null) throw new IllegalStateException("Input parameter user == null");
		for (Permission perm : permissions) {
			if (perm.getUser().equals(user) && perm.getPermissionType().equals(type)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Returns true if this key is a primaryKey, false otherwise
	 * @return
	 */
	public boolean isPrimaryKey() {
		if (this.getUser().getPrimaryKey().equals(this)) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Deletes this Key from the Database
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key is no PrimaryKey
	 */
	public void delete() throws InsufficientPermissionsException {
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().equals(user.getPrimaryKey())) {
				
				user.getKeys().remove(this);
				Session session = Application.getInstance().getSessionFactory().getCurrentSession();
				session.delete(this);
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	
	/**
	 * Adds a Permission for the supplied Lambda of the supplied type to this Key Object
	 * @param lambda the supplied Lambda
	 * @param type  the supplied Type
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 * Permissions
	 */
	public void grantPermission(Lambda lambda, PermissionType type) throws InsufficientPermissionsException {
		if (currentAuthenticatedUserHasLambdaPermissionToGrant(lambda, type)) {
			Permission permission = new Permission(lambda, type);
			
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.beginTransaction();
			permissions.add(permission);
			session.save(permissions);
			session.getTransaction().commit();
		}
		throw new InsufficientPermissionsException();
	}
	
	
	/**
	 * Adds a Permission for the supplied User of the supplied type to this Key Object
	 * @param user the supplied User
	 * @param type  the supplied Type
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 * Permissions
	 */
	public void grantPermission(User user, PermissionType type) throws InsufficientPermissionsException {
		if (currentAuthenticatedUserHasUserPermissionToGrant(user, type)) {
			Permission permission = new Permission(user, type);
			
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.beginTransaction();
			permissions.add(permission);
			session.save(permissions);
			session.getTransaction().commit();
		}
		throw new InsufficientPermissionsException();
	}
	
	
	/**
	 * Removes a Permission for the supplied Lambda of the supplied type to this Key Object
	 * @param lambda the supplied Lambda
	 * @param type  the supplied Type
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 * Permissions
	 */
	public void revokePermission(Lambda lambda, PermissionType type) throws InsufficientPermissionsException {
		if (currentAuthenticatedUserHasLambdaPermissionToGrant(lambda, type)) {
			Permission permission = new Permission(lambda, type);
			permissions.remove(permission);
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.delete(permission);
		}
		throw new InsufficientPermissionsException();
	}
	
	
	/**
	 * Removes a Permission for the supplied User of the supplied type to this Key Object
	 * @param user the supplied User
	 * @param type  the supplied Type
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 * Permissions
	 */
	public void revokePermission(User user, PermissionType type) throws InsufficientPermissionsException {
		if (currentAuthenticatedUserHasUserPermissionToGrant(user, type)) {
			Permission permission = new Permission(user, type);
			permissions.remove(permission);
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.delete(permission);
		}
		throw new InsufficientPermissionsException();
	}
	
	
	private boolean currentAuthenticatedUserHasLambdaPermissionToGrant(Lambda lambda, PermissionType type) {
		
		if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
				.hasPermission(lambda, PermissionType.GRANT)) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
					.hasPermission(lambda, type)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	private boolean currentAuthenticatedUserHasUserPermissionToGrant(User user, PermissionType type) {
		
		if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
				.hasPermission(user, PermissionType.GRANT)) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
					.hasPermission(user, type)) {
				return true;
			}
		}
		return false;
	}
}
