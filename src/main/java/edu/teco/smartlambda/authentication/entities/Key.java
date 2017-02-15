package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.lambda.Lambda;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Set;

/**
 * Created by Matteo on 01.02.2017.
 */
public class Key {
	
	private String          id;
	private String          name;
	private User            user;
	private Set<Permission> permissions;
	
	@Id
	@Column(name = "id", unique = true, nullable = false)
	private String getId() {
		return id;
	}
	
	private void setId(final String id) {
		this.id = id;
	}
	
	@Column(name = "name", nullable = false)
	public String getName() {
		return name;
	}
	
	private void setName(final String name) {
		this.name = name;
	}
	
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "User", nullable = false)
	public User getUser() {
		return user;
	}
	
	private void setUser(User user) {
		this.user = user;
	}
	
	@OneToMany(fetch = FetchType.LAZY,mappedBy = "Key")
	private Set<Permission> getPermissions() {
		return null;
	}
	
	private void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}
	
	public boolean hasPermission(Lambda lambda, PermissionType type) {
		
		for (Permission perm : this.getPermissions()) {
			if (perm.getLambda().equals(lambda) && perm.getPermissionType().equals(type)) {
					return true;
			}
		}
		return false;
	}
	
	public boolean hasPermission(User user, PermissionType type) {
		
		for (Permission perm : this.getPermissions()) {
			if (perm.getUser().equals(user) && perm.getPermissionType().equals(type)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPrimaryKey() {
		if (this.getUser().getPrimaryKey().equals(this)) {
			return true;
		}
		return false;
	}
	
	public void delete() throws InsufficientPermissionsException{
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().equals(user.getPrimaryKey())) {
				//TODO delete Key from Database
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	public void grantPermission(Lambda lambda, PermissionType type) throws InsufficientPermissionsException{
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().hasPermission(lambda, PermissionType.GRANT)) {
				if (AuthenticationService.getInstance().getAuthenticatedKey().get().hasPermission(lambda, type)) {
					Permission permission = new Permission(lambda, type);
					permissions.add(permission);
					//TODO set permission in Database
				}
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	public void grantPermission(User user, PermissionType type) throws InsufficientPermissionsException {
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().hasPermission(user, PermissionType.GRANT)) {
				if (AuthenticationService.getInstance().getAuthenticatedKey().get().hasPermission(user, type)) {
					Permission permission = new Permission(user, type);
					permissions.add(permission);
					//TODO set permission in Database
				}
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	public void revokePermission(Lambda lambda, PermissionType type) throws InsufficientPermissionsException{
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().hasPermission(lambda, PermissionType.GRANT)) {
				if (AuthenticationService.getInstance().getAuthenticatedKey().get().hasPermission(lambda, type)) {
					Permission permission = new Permission(lambda, type);
					permissions.remove(permission);
					//TODO remove permission in Database
				}
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	public void revokePermission(User user, PermissionType type) throws InsufficientPermissionsException{
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().hasPermission(user, PermissionType.GRANT)) {
				if (AuthenticationService.getInstance().getAuthenticatedKey().get().hasPermission(user, type)) {
					Permission permission = new Permission(user, type);
					permissions.remove(permission);
					//TODO remove permission in Database
				}
			}
		}
		throw new InsufficientPermissionsException();
	}
	
}
