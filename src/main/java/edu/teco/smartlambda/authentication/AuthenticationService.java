package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Manages authentication of Keys inside a thread. Between different threats the authentication runs independently. Before a Key is
 * authenticated, getter-methods will return empty optionals. Otherwise getter-methods base their return values on the last authenticated
 * Key
 */
public class AuthenticationService {
	
	private static final ThreadLocal<AuthenticationService> instance         = ThreadLocal.withInitial(AuthenticationService::new);
	private              Key                                authenticatedKey = null;
	
	/**
	 * The AuthenticationService is managed as a ThreadLocal Singleton. getInstance() returns this threads instance of the
	 * AuthenticationService.
	 * @return AuthenticationService the current Threads AuthenticationService Instance
	 */
	public static AuthenticationService getInstance() {
		return instance.get();
	}
	
	private AuthenticationService() {
		
	}
	
	/**
	 * Finds the corresponding Key and sets it as the currently authenticated Key
	 * @param key unhashed ID of the Key which is meant to be authenticated
	 */
	public void authenticate(final String key) {
		final String hash;
		try {
			final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			hash = Hex.encodeHexString(sha256.digest(key.getBytes()));
		} catch (final NoSuchAlgorithmException a) {
			throw new RuntimeException(a);
		}
		
		this.authenticatedKey = Key.getKeyById(hash).orElseThrow(NotAuthenticatedException::new);
	}
	
	/**
	 * Sets Key as the currently authenticated Key
	 * @param key Key which is meant to authenticate
	 */
	public void authenticate(final Key key) {
		this.authenticatedKey = key;
	}
	
	/**
	 * Returns an Optional, which contains the currently authenticated Key Object
	 * @return Optional, is empty if there is no authenticated Key
	 */
	public Optional<Key> getAuthenticatedKey() {
		return Optional.ofNullable(this.authenticatedKey);
	}
	
	/**
	 * Returns an Optional, which contains the currently authenticated Keys User Object
	 * @return Optional, is empty if there is no authenticated Key
	 */
	public Optional<User> getAuthenticatedUser() {
		if (this.authenticatedKey != null) {
			return Optional.of(this.authenticatedKey.getUser());
		}
		return Optional.empty();
	}
}
