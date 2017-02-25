package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NameConflictException;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Set;

import static javafx.beans.binding.Bindings.select;
import static javax.persistence.GenerationType.IDENTITY;
import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

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
	private Set<Key> keys;
		
	public User() {
		
		try {
			this.primaryKey = addKey(this.name).getLeft();
		} catch (NameConflictException e){
			// This is the first Key of this User, there cannot be another Key with the same name
		}
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
	 * Creates a new Key Object and adds it to the Database
	 * @param name Name for the Key
	 * @return Pair of the Key object and the Keys ID as a String
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key is no PrimaryKey
	 * @throws NameConflictException If the Name is already used for a key of this User
	 */
	public Pair<Key, String> createKey(String name) throws InsufficientPermissionsException, NameConflictException {
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().equals(this.getPrimaryKey())) {
				
				return addKey(name);
				
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	private Pair<Key, String> addKey(String name) throws NameConflictException {
		
		//TODO: query mit dem Namen, wenn es schon einen gibt wirft es eine Exception. Wenn der User nicht vorliegt gibt's auch keine
		// Exception
				
		String generatedNumber = "";
		String id = "";
		//TODO: generate number and hash it
		Key key = new Key(id, name, this);
		
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		keys.add(key);
		session.save(keys);
		session.getTransaction().commit();
		
		return Pair.of(key, generatedNumber);
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
	
	public Set<Key> getKeys() { return keys; }
	
	/**
	 * Retrieve a single User by its name
	 *
	 * @param name User name
	 *
	 * @return a User object
	 */
	public static User getByName(final String name) {
		
		final User query = from(User.class);
		where(query.getName()).eq(name);
		return select(query).get(Application.getInstance().getSessionFactory().getCurrentSession());
	}
}
