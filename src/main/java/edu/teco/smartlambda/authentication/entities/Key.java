package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NameNotFoundException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.lambda.Lambda;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.torpedoquery.jpa.Query;

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
	private String          id;
	@Getter
	@Setter
	@Column(name = "name", nullable = false)
	private String          name;
	@Getter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="user")
	private User            user;

	
	
	Key() {
		
	}
	
	Key(String id, String name, User user) {
		
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.getTransaction();
		
		this.id = id;
		this.name = name;
		this.user = user;
		
		
		session.save(this);
		session.getTransaction().commit();
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
	 * @param lambda a Permission has to be a Permission for this Lambda
	 * @param type a Permission has to be a Permission of this Type
	 * @return returns if the queried Permission exists
	 */
	public boolean hasPermission(Lambda lambda, PermissionType type) {
		final Permission permission = from(Permission.class);
		where(permission.getKey()).eq(this).and(permission.getLambda()).eq(lambda).and(permission.getPermissionType()).eq(type);
		final Query<Permission> permissionQuery = select(permission);
		//TODO-ASK Sicherstellung, dass nur eine Permission der selben Art vorhanden ist.
		//return select(permission).get(Application.getInstance().getSessionFactory().getCurrentSession()).isPresent();
		return select(permission).list(Application.getInstance().getSessionFactory().getCurrentSession()).isEmpty();
	}
	
	
	/**
	 * Checks if the Key has the Permission of the supplied PermissionType for the supplied User
	 * @param user a Permission has to be a Permission for this User
	 * @param type a Permission has to be a Permission of this Type
	 * @return returns if the queried Permission exists
	 */
	public boolean hasPermission(User user, PermissionType type) {
		final Permission permission = from(Permission.class);
		where(permission.getKey()).eq(this).and(permission.getUser()).eq(user).and(permission.getPermissionType()).eq(type);
		final Query<Permission> permissionQuery = select(permission);
		//TODO-ASK Sicherstellung, dass nur eine Permission der selben Art vorhanden ist.
		//return select(permission).get(Application.getInstance().getSessionFactory().getCurrentSession()).isPresent();
		return select(permission).list(Application.getInstance().getSessionFactory().getCurrentSession()).isEmpty();
	}
	
	
	/**
	 * Returns true if this key is a primaryKey, false otherwise
	 * @return
	 */
	@Transient
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
		Key authenticatedKey = AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new);
		if (authenticatedKey.equals(user.getPrimaryKey())) {
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			//TODO what to do when the primaryKey deletes itself: delete User too?
			session.delete(this);
			session.getTransaction().commit();
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
			grantPermission(new Permission(lambda, type, this));
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
			grantPermission(new Permission(user, type, this));
		}
		throw new InsufficientPermissionsException();
	}
	
	private void grantPermission(Permission permission) {
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.save(permission);
		session.getTransaction().commit();
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
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			//TODO-ASK Sicherstellung, dass nur eine Permission der selben Art vorhanden ist.
			final Permission permission = from(Permission.class);
			where(permission.getKey()).eq(this).and(permission.getLambda()).eq(lambda).and(permission.getPermissionType()).eq(type);
			List<Permission> permissions = select(permission).list(session);
			for (Permission perm : permissions) {
				session.delete(perm);
			}
			session.getTransaction().commit();
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
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			//TODO-ASK Sicherstellung, dass nur eine Permission der selben Art vorhanden ist.
			final Permission permission = from(Permission.class);
			where(permission.getKey()).eq(this).and(permission.getUser()).eq(user).and(permission.getPermissionType()).eq(type);
			List<Permission> permissions = select(permission).list(session);
			for (Permission perm : permissions) {
				session.delete(perm);
			}
			session.getTransaction().commit();
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
	
	public static Key getKeyById(String id) throws NameNotFoundException{
		final Key query = from(Key.class);
		where(query.getId()).eq(id);
		return select(query).get(Application.getInstance().getSessionFactory().getCurrentSession()).orElseThrow
				(NameNotFoundException::new);
	}
}
