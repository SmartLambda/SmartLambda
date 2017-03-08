package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.DuplicateKeyException;
import edu.teco.smartlambda.authentication.DuplicateUserException;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.persistence.GenerationType.IDENTITY;
import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 *
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
	
	/**
	 * Creates a new User object and new Key Object and adds it to the Database as it's primaryKey
	 * @param name System wide unique name of the new User
	 * @return Pair of the new User object and the unhashed id of the created primaryKey
	 */
	public static Pair<User, String> createUser(final String name) throws DuplicateUserException {
		if (User.getByName(name).isPresent()) throw new DuplicateUserException("Username is already used");
		final User user = new User(name);
		Application.getInstance().getSessionFactory().getCurrentSession().save(user);
		Pair<Key, String> pair = null; //This will not stay null in any case
		try {
			pair = user.addKey(user.name);
		} catch (final DuplicateKeyException e) {
			// This is the first Key of this User, there cannot be another Key with the same name
			assert false;
		}
		user.primaryKey = pair.getLeft();
		Application.getInstance().getSessionFactory().getCurrentSession().save(user);
		return Pair.of(user, pair.getRight());
	}
	
	private User(final String name) {
		this.name = name;
	}
	
	/**
	 * Sets the flag for this User to be an Admin
	 * @param admin true, if this user shall be an Admin
	 */
	void setAdmin(final boolean admin) {
		this.isAdmin = admin;
		Application.getInstance().getSessionFactory().getCurrentSession().save(this);
	}
	
	/**
	 * Creates a new Key Object and adds it to the Database
	 *
	 * @param name Name for the Key
	 *
	 * @return Pair of the Key object and the Keys unhashed ID as a String
	 *
	 * @throws InsufficientPermissionsException if the current Threads authenticated Key is no PrimaryKey
	 * @throws DuplicateKeyException            If the Name is already used for a key of this User
	 */
	public Pair<Key, String> createKey(final String name) throws InsufficientPermissionsException, DuplicateKeyException {
		if (AuthenticationService.getInstance().getAuthenticatedKey().isPresent()) {
			if (AuthenticationService.getInstance().getAuthenticatedKey().get().equals(this.getPrimaryKey())) {
				
				return this.addKey(name);
			}
		}
		throw new InsufficientPermissionsException();
	}
	
	private Pair<Key, String> addKey(final String name) throws DuplicateKeyException {
		final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		final Key     query   = from(Key.class);
		where(query.getName()).eq(name);
		final Optional<Key> keyOptional = select(query).get(session);
		if (keyOptional.isPresent()) {
			throw new DuplicateKeyException("Key name already used");
		}
		
		final String id;
		final String hash;
		final String generatedNumber = "" + Math.random();
		try {
			final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			
			hash = this.arrayToString(sha256.digest(generatedNumber.getBytes()));
			
			id = this.arrayToString(sha256.digest(hash.getBytes()));
		} catch (final NoSuchAlgorithmException a) {
			throw new RuntimeException(a);
		}
		final Key key = new Key(id, name, this);
		session.save(key);

		return Pair.of(key, hash);
	}
	
	private String arrayToString(final byte[] array)
	{
		final StringBuilder sb = new StringBuilder();
		for (final byte anArray : array) {
			sb.append(Integer.toHexString((anArray & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}
	
	/**
	 * Returns all Users, which this User can See (all Users if this User is an Admin and Users with shared Lambdas otherwise)
	 *
	 * @return Set of Users
	 */
	@Transient
	public Set<User> getVisibleUsers() {
		final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
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
			
			final Set<User> userSet = new HashSet<>();
			
			for (final Permission perm : permissionSet) {
				if (perm.getUser()!= null) {
					userSet.add(perm.getUser());
				} else if (perm.getLambda() != null) {
					userSet.add(perm.getLambda().getOwner());
				}
			}
			
			return userSet;
		}
	}
	
	private void ensureAuthenticatedWithPrimaryKey() {
		if (!AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new).isPrimaryKey())
			throw new InsufficientPermissionsException();
	}
	
	@Transient
	public List<AbstractLambda> getLambdas() {
		final Lambda query = from(Lambda.class);
		where(query.getOwner()).eq(this);
		return select(query).list(Application.getInstance().getSessionFactory().getCurrentSession()).stream()
				.map(lambda -> LambdaFacade.getInstance().getFactory().decorate(lambda)).collect(Collectors.toList());
	}
	
	/**
	 * Returns all lambdas of this user that are visible to the currently authenticated user
	 *
	 * @return set of lambdas
	 * @throws InsufficientPermissionsException when user is not authenticated with their primary key
	 */
	@Transient
	public Set<AbstractLambda> getVisibleLambdas() {
		this.ensureAuthenticatedWithPrimaryKey();
		
		final Set<AbstractLambda> lambdas = new HashSet<>();
		
		assert AuthenticationService.getInstance().getAuthenticatedUser().isPresent();
		
		for (final AbstractLambda lambda : this.getLambdas()) {
			if (AuthenticationService.getInstance().getAuthenticatedUser().get().getPrimaryKey().hasPermission(lambda, PermissionType
					.READ))
				lambdas.add(lambda);
		}
		
		return lambdas;
	}
	
	/**
	 * Returns Keys of this User with the supplied Name
	 *
	 * @return Set of Keys
	 */
	@Transient
	public Optional<Key> getKeyByName(final String name) {
		this.ensureAuthenticatedWithPrimaryKey();
		
		final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		final Key     query   = from(Key.class);
		where(query.getUser()).eq(this).and(query.getName()).eq(name);
		return select(query).get(session);
	}
	
	/**
	 * Retrieve a single User by its name
	 *
	 * @param name User name
	 *
	 * @return a User object
	 */
	public static Optional<User> getByName(final String name) {
		final User query = from(User.class);
		where(query.getName()).eq(name);
		return select(query).get(Application.getInstance().getSessionFactory().getCurrentSession());
	}
}
