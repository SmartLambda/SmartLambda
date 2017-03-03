package edu.teco.smartlambda.authentication.entities;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NameConflictException;
import edu.teco.smartlambda.authentication.NameNotFoundException;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.torpedoquery.jpa.Query;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
	@Getter
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private int     id;
	@Getter
	@Column(name = "name", unique = true, nullable = false)
	private String  name;
	@Getter
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "primaryKey")
	private Key     primaryKey;
	@Getter
	@Column(name = "isAdmin", nullable = false)
	private boolean isAdmin;
	
	public User() {
		
	}
	
	public static Pair<User, String> createUser(String name) {
		User user = new User(name);
		Pair<Key, String> pair = null; //This will not stay null in any case
		try {
			pair = user.addKey(user.name);
		} catch (NameConflictException e) {
			// This is the first Key of this User, there cannot be another Key with the same name
		}
		user.primaryKey = pair.getLeft();
		Application.getInstance().getSessionFactory().getCurrentSession().save(user);
		return Pair.of(user, pair.getRight());
	}
	
	private User(String name) {
		this.name = name;
	}
	
	private void setName(final String name) {
		this.name = name;
	}
	
	private void setPrimaryKey(final Key primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	void setAdmin(final boolean admin) {
		isAdmin = admin;
		Application.getInstance().getSessionFactory().getCurrentSession().save(this);
	}
	
	/**
	 * Creates a new Key Object and adds it to the Database
	 *
	 * @param name Name for the Key
	 *
	 * @return Pair of the Key object and the Keys ID as a String
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key is no PrimaryKey
	 * @throws NameConflictException            If the Name is already used for a key of this User
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
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		final Key query = from(Key.class);
		where(query.getName()).eq(name);
		final Optional<Key> keyOptional = select(query).get(session);
		if (keyOptional.isPresent()) {
			throw new NameConflictException();
		}
		
		String id;
		String hash;
		String generatedNumber = "" + Math.random();
		Argon2 argon2          = Argon2Factory.create();
		
		// Hash generatedNumber
		hash = argon2.hash(2, 65536, 1, generatedNumber);
		
		// Verify generatedNumber
		if (!argon2.verify(hash, generatedNumber)) {
			throw new RuntimeException("hash doesn't match generatedNumber");
		}
		
		id = argon2.hash(2, 65536, 1, hash);
		
		// Verify hash
		if (!argon2.verify(id, hash)) {
			throw new RuntimeException("id doesn't match hash");
		}
		
		Key key = new Key(id, name, this);
		session.save(key);
		
		return Pair.of(key, hash);
	}
	
	/**
	 * Returns all Users, which this User can See (all Users if this User is an Admin and Users with shared Lambdas otherwise)
	 *
	 * @return Set of Users
	 */
	@Transient
	public Set<User> getVisibleUsers() {
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		if (this.isAdmin) {
			final User query = from(User.class);
			return new HashSet<>(select(query).list(session));
		} else {
			final Key key = from(Key.class);
			where(key.getUser()).eq(this);
			final Query<Key> keyQuery = select(key);
			
			final Permission permission = from(Permission.class);
			where(permission.getKey()).in(keyQuery);
			final Set<Permission> permissionSet =
					new HashSet<>(select(permission).list(session));
			
			Set<User> userSet = new HashSet<>();
			
			for (Permission perm : permissionSet) {
				if (perm.getUser()!= null) {
					userSet.add(perm.getUser());
				} else if (perm.getLambda() != null) {
					userSet.add(perm.getLambda().getOwner());
				}
			}
			
			return userSet;
		}
	}
	
	/**
	 * Retrieve a single User by its name
	 *
	 * @param name User name
	 *
	 * @return a User object
	 */
	public static User getByName(final String name) throws NameNotFoundException {
		final User query = from(User.class);
		where(query.getName()).eq(name);
		return select(query).get(Application.getInstance().getSessionFactory().getCurrentSession()).orElseThrow
				(NameNotFoundException::new);
	}
}
