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
import org.hibernate.query.Query;

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
	
	/**
	 * Empty constructor, used by Hibernate
	 */
	Key() {
		
	}
	
	/**
	 * Creates a new Key
	 *
	 * @param id   The id, the key can be found in the database with
	 * @param name Human readable identifier, unique per User
	 * @param user This key is assigned to that user
	 */
	Key(final String id, final String name, final User user) {
		this.id = id;
		this.name = name;
		this.user = user;
	}
	
	/**
	 * Returns all permissions of the key
	 *
	 * @return Set of Permissions
	 */
	public Set<Permission> getPermissions() {
		final Permission permission = from(Permission.class);
		where(permission.getKey()).eq(this);
		
		return new HashSet<>(select(permission).list(Application.getInstance().getSessionFactory().getCurrentSession()));
	}
	
	/**
	 * Returns a Set of Permissions of the following Types:
	 * 1. User-Permissions of this Key for the currently authenticated User
	 * 2. Lambda-Permissions of this Key for the currently authenticated Users Lambdas
	 * 3. User-Permissions of this Key, sharing a User with the currently authenticated Keys GRANT-Permissions
	 * 4. Lambda-Permissions of this Key, sharing a Lambda with the currently authenticated Keys GRANT-Permissions
	 * @return Set of those Permissions
	 */
	public Set<Permission> getVisiblePermissions() {
		final User authenticatedUser =
				AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new);
		final HashSet<Permission> permissions = new HashSet<>();
		
		final Permission permissionUser = from(Permission.class);
		where(permissionUser.getKey()).eq(this).and(permissionUser.getUser()).eq(authenticatedUser);
		permissions.addAll(select(permissionUser).list(Application.getInstance().getSessionFactory().getCurrentSession()));
		
		final Permission permissionLambda = from(Permission.class);
		where(permissionLambda.getKey()).eq(this).and(permissionLambda.getLambda().getOwner()).eq(authenticatedUser);
		permissions.addAll(select(permissionLambda).list(Application.getInstance().getSessionFactory().getCurrentSession()));
		
		final Query<Permission> permissionGrantUser = Application.getInstance().getSessionFactory().getCurrentSession().createQuery(
				"SELECT p1 FROM edu.teco.smartlambda.authentication.entities.Permission p1 WHERE p1.key = :key AND EXISTS (SELECT 1 FROM" +
						" " + "edu" + ".teco" + ".smartlambda.authentication.entities.Permission p2 WHERE " +
						"p2.key = :authenticatedKey AND p2.permissionType = 'GRANT' AND p2.user = p1.user)", Permission.class);
		permissionGrantUser.setParameter("key", this);
		
		assert AuthenticationService.getInstance().getAuthenticatedKey().isPresent();
		permissionGrantUser.setParameter("authenticatedKey", AuthenticationService.getInstance().getAuthenticatedKey().get());
		permissions.addAll(permissionGrantUser.getResultList());
		
		final Query<Permission> permissionGrantLambda = Application.getInstance().getSessionFactory().getCurrentSession().createQuery(
				"SELECT p1 FROM edu.teco.smartlambda.authentication.entities.Permission p1 WHERE p1.key = :key AND EXISTS " +
						"(SELECT 1 FROM edu.teco.smartlambda.authentication.entities.Permission p2 WHERE " +
						"p2.key = :authenticatedKey AND p2.permissionType = 'GRANT' AND p2.lambda = p1.lambda)", Permission.class);
		permissionGrantLambda.setParameter("key", this);
		permissionGrantLambda.setParameter("authenticatedKey", AuthenticationService.getInstance().getAuthenticatedKey().get());
		permissions.addAll(permissionGrantLambda.getResultList());
		
		return permissions;
	}
	
	/**
	 * Checks if the Key has the Permission of the supplied PermissionType for the supplied Lambda
	 * Returns always true, if the key is the primaryKey of the supplied Lambdas User
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
		return !select(permission).list(Application.getInstance().getSessionFactory().getCurrentSession()).isEmpty() ||
				this.hasPermission(lambda.getOwner(), type);
	}
	
	/**
	 * Checks if the Key has the Permission of the supplied PermissionType for the supplied User
	 * Returns always true, if the key is the primaryKey of the supplied User
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
			Application.getInstance().getSessionFactory().getCurrentSession().delete(this);
			if (this.isPrimaryKey()) Application.getInstance().getSessionFactory().getCurrentSession().delete(this.getUser());
		} else {
			throw new InsufficientPermissionsException();
		}
	}
	
	/**
	 * Adds a Permission for the supplied lambda of the supplied type to this Key Object
	 * Adding an already existing Permission or a Permission where the equivalent User-Permission exists, doesn't change anything
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
	 * Adding an already existing Permission doesn't change anything
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
	 * If the Permission doesn't exist, nothing changes
	 *
	 * @param lambda the supplied Lambda
	 * @param type   the supplied Type
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 *                                          Permissions
	 */
	public void revokePermission(final AbstractLambda lambda, final PermissionType type) throws InsufficientPermissionsException {
		if (this.currentAuthenticatedUserHasLambdaPermissionToGrant(LambdaDecorator.unwrap(lambda), type)) {
			final Session    session    = Application.getInstance().getSessionFactory().getCurrentSession();
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
	 * If the Permission doesn't exist, nothing changes
	 *
	 * @param user the supplied User
	 * @param type the supplied Type
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key has no Permission to Grant this kind of
	 *                                          Permissions
	 */
	public void revokePermission(final User user, final PermissionType type) throws InsufficientPermissionsException {
		if (this.currentAuthenticatedUserHasUserPermissionToGrant(user, type)) {
			final Session    session    = Application.getInstance().getSessionFactory().getCurrentSession();
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
	
	/**
	 * Searches in the database for a Key object with the supplied id
	 *
	 * @param id id parameter of the key
	 *
	 * @return an optional with the found key or an empty Optional if such a key doesn't exist
	 */
	public static Optional<Key> getKeyById(final String id) {
		final Key query = from(Key.class);
		where(query.getId()).eq(id);
		return select(query).get(Application.getInstance().getSessionFactory().getCurrentSession());
	}
}
