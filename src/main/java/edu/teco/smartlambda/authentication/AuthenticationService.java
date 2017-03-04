package edu.teco.smartlambda.authentication;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;

import java.util.Optional;

/**
 * Created on 01.02.17.
 */
public class AuthenticationService {
	
	private static ThreadLocal<AuthenticationService> instance         = null;
	private        Key                                authenticatedKey = null;
	
	/**
	 * The AuthenticationService is managed as a ThreadLocal Singleton.
	 * @return AuthenticationService the current Threads AuthenticationService Instance
	 */
	public static AuthenticationService getInstance() {
		if (instance == null) {
			instance = new ThreadLocal<>();
			instance.set(new AuthenticationService());
		} else if (instance.get() == null) {
			instance.set(new AuthenticationService());
		}
		
		return instance.get();
	}
	
	private AuthenticationService() {
		
	}
	
	/**
	 * Finds the corresponding Key and sets it as the currently authenticated Key
	 * @param key ID of the Key which is meant to authenticate
	 */
	public void authenticate(final String key) {
		String hash;
		Argon2 argon2 = Argon2Factory.create();
		
		// Hash key
		hash = argon2.hash(2, 65536, 1, key);
		
		// Verify hash
		if (!argon2.verify(hash, key)) {
			throw new RuntimeException("hash doesn't match key");
		}
		authenticatedKey = Key.getKeyById(hash).orElseThrow(NotAuthenticatedException::new);
	}
	
	/**
	 * Sets Key as the currently authenticated Key
	 * @param key Key which is meant to authenticate
	 */
	public void authenticate(final Key key) {
		authenticatedKey = key;
	}
	
	/**
	 * Returns an Optional, which contains the authenticated Key Object
	 * @return Optional
	 */
	public Optional<Key> getAuthenticatedKey() {
		return Optional.ofNullable(authenticatedKey);
	}
	
	/**
	 * Returns an Optional, which contains the authenticated Keys User Object
	 * @return Optional
	 */
	public Optional<User> getAuthenticatedUser() {
		if (authenticatedKey != null) {
			return Optional.of(authenticatedKey.getUser());
		}
		return Optional.empty();
	}
}
