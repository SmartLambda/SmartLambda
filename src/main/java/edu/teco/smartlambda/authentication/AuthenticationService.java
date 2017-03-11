package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Created on 01.02.17.
 */
public class AuthenticationService {
	
	private static final ThreadLocal<AuthenticationService> instance         = ThreadLocal.withInitial(AuthenticationService::new);
	private              Key                                authenticatedKey = null;
	
	/**
	 * The AuthenticationService is managed as a ThreadLocal Singleton.
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
			hash = this.arrayToString(sha256.digest(key.getBytes()));
		} catch (final NoSuchAlgorithmException a) {
			throw new RuntimeException(a);
		}
		
		this.authenticatedKey = Key.getKeyById(hash).orElseThrow(NotAuthenticatedException::new);
	}
	
	private String arrayToString(final byte[] array)
	{
		final StringBuilder sb = new StringBuilder();
		for (final byte currentByte : array) {
			sb.append(Integer.toHexString((currentByte & 0xFF) | 0x100).substring(1, 3));
		}
		return sb.toString();
	}
	
	/**
	 * Sets Key as the currently authenticated Key
	 * @param key Key which is meant to authenticate
	 */
	public void authenticate(final Key key) {
		this.authenticatedKey = key;
	}
	
	/**
	 * Returns an Optional, which contains the authenticated Key Object
	 * @return Optional, is empty if there is no authenticated Key
	 */
	public Optional<Key> getAuthenticatedKey() {
		return Optional.ofNullable(this.authenticatedKey);
	}
	
	/**
	 * Returns an Optional, which contains the authenticated Keys User Object
	 * @return Optional, is empty if there is no authenticated Key
	 */
	public Optional<User> getAuthenticatedUser() {
		if (this.authenticatedKey != null) {
			return Optional.of(this.authenticatedKey.getUser());
		}
		return Optional.empty();
	}
}
