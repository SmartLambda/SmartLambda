package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NameConflictException;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Created by Matteo on 01.02.2017.
 */
@Entity
@Table(name = "User")
public class User {
	

	private int      id;
	private String   name;
	private Key      primaryKey;
	private boolean  isAdmin;
		
	public User() {
		//TODO (Git-Hub) authentication.
	}
	
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
	 * Returns the Name of this User
	 * @return name
	 */
	@Column(name = "name", unique = true, nullable = false)
	public String getName() {
		return name;
	}
	
	private void setName(final String name) {
		this.name = name;
	}
	
	/**
	 * Returns the PrimaryKey of this User
	 * @return PrimaryKey
	 */
	@OneToOne(fetch = FetchType.LAZY,mappedBy = "User")
	public Key getPrimaryKey() {
		return primaryKey;
	}
	
	private void setPrimaryKey(final Key primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	/**
	 * Returns true if this User is a Admin-User, false otherwise
	 * @return Result
	 */
	@Column(name = "isAdmin",nullable = false)
	public boolean isAdmin() {
		return isAdmin;
	}
	
	private void setAdmin(final boolean admin) {
		isAdmin = admin;
	}
	
	/**
	 * Creates a new Cey Object and adds it to the Database
	 * @param name Name for the Key
	 * @return Pair of the Key object and the Keys ID as a String
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key is no PrimaryKey
	 * @throws NameConflictException If the Name is already used for this User
	 */
	public Pair<Key, String> createKey(String name) throws InsufficientPermissionsException, NameConflictException {
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().equals(this.getPrimaryKey())) {
				//TODO create Key and return it
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	/**
	 * Returns all Users, which this User can See (all Users if this User is an Admin and Users with shared Lambdas otherwise)
 	 * @return Set of Users
	 */
	public Set<User> getVisibleUsers() {
		if (this.isAdmin) {
			//TODO return all Users
		} else {
			//TODO search in database for all own keys and all of their permissions for foreign Users. return them as a Set.
		}
		return null;
	}
}
