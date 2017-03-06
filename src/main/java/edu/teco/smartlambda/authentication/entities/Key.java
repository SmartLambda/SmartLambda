package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.LambdaDecorator;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * Created on 01.02.2017.
 */
@Entity
@Table(name = "Key")
public class Key {
	@Getter
	@Id
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	@Getter
	@Setter
	@Column(name = "name", nullable = false)
	private String name;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user")
	private User   user;
	
	Key() {
		
	}
	
	Key(final String id, final String name, final User user) {
		this.id = id;
		this.name = name;
		this.user = user;
	}
	
	private void setId(final String id) {
		this.id = id;
	}
	
	private void setUser(final User user) {
		this.user = user;
	}
	
	public Set<Permission> getPermissions() {
		final Permission permission = from(Permission.class);
		where(permission.getKey()).eq(this);
		
		return new HashSet<>(select(permission).list(Application.getInstance().getSessionFactory().getCurrentSession()));
	}
	
	/**
	 * Checks if the Key has the Permission of the supplied PermissionType for the supplied Lambda
	 *
	 * @param lambda a Permission has to be a Permission for this Lambda
	 * @param type   a Permission has to be a Permission of this Type
	 *
	 * @return returns if the queried Permission exists
	 */
	public boolean hasPermission(final AbstractLambda lambda, final PermissionType type) {
		if (this.equals(lambda.getOwner().getPrimaryKey())) return true;
		final Permission permission = from(Permission.class);
		where(permission.getKey()).eq(this).and(permission.getLambda()).eq(LambdaDecorator.unwrap(lambda))
				.and(permission.getPermissionType()).eq(type);
		return !select(permission).list(Application.getInstance().getSessionFactory().getCurrentSession()).isEmpty();
	}
	
	/**
	 * Checks if the Key has the Permission of the supplied PermissionType for the supplied User
	 *
	 * @param user a Permission has to be a Permission for this User
	 * @param type a Permission has to be a Permission of this Type
	 *
	 * @return returns if the queried Permission exists
	 */
	public boolean hasPermission(final User user, final PermissionType type) {
		if (this.equals(user.getPrimaryKey())) return true;
		final Permission permission = from(Permission.class);
		where(permission.getKey()).eq(this).and(permission.getUser()).eq(user).and(permission.getPermissionType()).eq(type);
		return !select(permission).list(Application.getInstance().getSessionFactory().getCurrentSession()).isEmpty();
	}
	
	/**
	 * Returns true if this key is a primaryKey, false otherwise
	 *
	 * @return value
	 */
	@Transient
	public boolean isPrimaryKey() {
		return this.getUser().getPrimaryKey().equals(this);
	}
	
	/**
	 * Deletes this Key from the Database
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key is no PrimaryKey
	 */
	public void delete() throws InsufficientPermissionsException {
		final Key authenticatedKey = AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new);
		if (authenticatedKey.equals(this.user.getPrimaryKey())) {
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.delete(this);
			session.delete(this.getUser());
		} else {
			throw new InsufficientPermissionsException();
		}
	}
	
	/**
	 * Adds a Permission for the supplied lambda of the supplied type to this Key Object
	 *
	 * @param lambda the supplied Lambda
	 * @param type   the supplied Type
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 *                                          Permissions
	 */
	public void grantPermission(final AbstractLambda lambda, final PermissionType type) throws InsufficientPermissionsException {
		if (this.hasPermission(lambda, type)) return;
		if (this.currentAuthenticatedUserHasLambdaPermissionToGrant(LambdaDecorator.unwrap(lambda), type)) {
			final Permission permission = new Permission(lambda, type, this);
			this.grantPermission(permission);
			Application.getInstance().getSessionFactory().getCurrentSession().save(permission);
		} else {
			throw new InsufficientPermissionsException();
		}
	}
	
	/**
	 * Adds a Permission for the supplied User of the supplied type to this Key Object
	 *
	 * @param user the supplied User
	 * @param type the supplied Type
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 *                                          Permissions
	 */
	public void grantPermission(final User user, final PermissionType type) throws InsufficientPermissionsException {
		if (this.hasPermission(user, type)) return;
		if (this.currentAuthenticatedUserHasUserPermissionToGrant(user, type)) {
			final Permission permission = new Permission(user, type, this);
			this.grantPermission(permission);
			Application.getInstance().getSessionFactory().getCurrentSession().save(permission);
		} else {
			throw new InsufficientPermissionsException();
		}
	}
	
	private void grantPermission(final Permission permission) {
		Application.getInstance().getSessionFactory().getCurrentSession().save(permission);
	}
	
	/**
	 * Removes a Permission for the supplied lambda of the supplied type to this Key Object
	 *
	 * @param lambda the supplied Lambda
	 * @param type   the supplied Type
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 *                                          Permissions
	 */
	public void revokePermission(final AbstractLambda lambda, final PermissionType type) throws InsufficientPermissionsException {
		if (this.currentAuthenticatedUserHasLambdaPermissionToGrant(LambdaDecorator.unwrap(lambda), type)) {
			final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			final Permission permission = from(Permission.class);
			where(permission.getKey()).eq(this).and(permission.getLambda()).eq(LambdaDecorator.unwrap(lambda))
					.and(permission.getPermissionType()).eq(type);
			final List<Permission> permissions = select(permission).list(session);
			for (final Permission perm : permissions) {
				session.delete(perm);
			}
		} else {
			throw new InsufficientPermissionsException();
		}
	}
	
	/**
	 * Removes a Permission for the supplied User of the supplied type to this Key Object
	 *
	 * @param user the supplied User
	 * @param type the supplied Type
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 *                                          Permissions
	 */
	public void revokePermission(final User user, final PermissionType type) throws InsufficientPermissionsException {
		if (this.currentAuthenticatedUserHasUserPermissionToGrant(user, type)) {
			final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			final Permission permission = from(Permission.class);
			where(permission.getKey()).eq(this).and(permission.getUser()).eq(user).and(permission.getPermissionType()).eq(type);
			final List<Permission> permissions = select(permission).list(session);
			for (final Permission perm : permissions) {
				session.delete(perm);
			}
		} else {
			throw new InsufficientPermissionsException();
		}
	}
	
	private boolean currentAuthenticatedUserHasLambdaPermissionToGrant(final AbstractLambda lambda, final PermissionType type) {
		if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
				.equals(lambda.getOwner().getPrimaryKey())) return true;
		if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
				.hasPermission(lambda, PermissionType.GRANT)) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
					.hasPermission(lambda, type)) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean currentAuthenticatedUserHasUserPermissionToGrant(final User user, final PermissionType type) {
		if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
				.equals(user.getPrimaryKey())) return true;
		if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
				.hasPermission(user, PermissionType.GRANT)) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
					.hasPermission(user, type)) {
				return true;
			}
		}
		return false;
	}
	
	public static Optional<Key> getKeyById(final String id) {
		final Key query = from(Key.class);
		where(query.getId()).eq(id);
		return select(query).get(Application.getInstance().getSessionFactory().getCurrentSession());
	}
}
