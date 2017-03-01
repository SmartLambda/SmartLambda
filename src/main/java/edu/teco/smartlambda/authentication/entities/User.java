package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NameConflictException;
import edu.teco.smartlambda.identity.IdentityException;
import edu.teco.smartlambda.identity.IdentityProvider;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Map;
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
	@PrimaryKeyJoinColumn
	private Key     primaryKey;
	@Getter
	@Column(name = "isAdmin", nullable = false)
	private boolean isAdmin;
		
	public User(Map<String, String> parameters) {
		
				
		//Der Id wird von der Datenbank gesetzt
		
		//TODO (Git-Hub) authentication
		//in this case the identificationToken is directly used as the name (instead of asking Git-Hub)
		IdentityProvider identityProvider = IdentityProviderRegistry.getInstance().getIdentityProviderByName("NullIdentityProvider");
		identityProvider.register(parameters);
		this.name = identityProvider.getName().orElseThrow(IdentityException::new);
		
		// Wie wird ermittelt, ob der User Administrator ist? Gibt's nur einen Admin? In dem Fall vllt mit einer Klassenvariable?
		try {
			this.primaryKey = addKey(this.name).getLeft();
		} catch (NameConflictException e){
			// This is the first Key of this User, there cannot be another Key with the same name
		}
	}
	
	private void setId(final int id) {
		this.id = id;
	}
	
	private void setName(final String name) {
		this.name = name;
	}
	
	private void setPrimaryKey(final Key primaryKey) {
		this.primaryKey = primaryKey;
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
		Set<Key> keys = new HashSet<>();
		for (Key key : keys) {
			if (key.getName().equals(name)) {
				//TODO: throw NameConflictException
			}
		}
				
		String generatedNumber = "";
		String id = "";
		//TODO: generate number and hash it
		//Der KeyId wird nicht von Hibernate generiert, ist das richtig? Wenn doch, können wir nicht den Id in der Datenbank speichern
		// und die gehashte Version zurückgeben?
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
	@Transient
	public Set<User> getVisibleUsers() {
		Set<Key> keys = new HashSet<>();
		if (this.isAdmin) {
			//TODO return all Users: Torpedo query list()
			return null;
		} else {
			Set<User> toReturn = new HashSet<>();
			for (Key key : keys) {
				for (Permission perm : key.getPermissions()) {
					
					if (perm.getUser() != null) {
						toReturn.add(perm.getUser());
					} else {
						toReturn.add(perm.getLambda().getOwner());
					}
					toReturn.remove(this); // Richtig??
				}
			}
			return toReturn;
		}
	}
	
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
