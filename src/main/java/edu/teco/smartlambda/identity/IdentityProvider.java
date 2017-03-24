package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * Provide any form of external identity verification
 */
public interface IdentityProvider {
	
	/**
	 * Registers a user with an external identification at the SmartLambda system
	 *
	 * @param parameters A set of parameters required for identity verification. Content depends on verification implementation
	 *
	 * @return A pair of a {@link User} and the name this user is identified with
	 *
	 * @throws IdentityException if the verification process fails
	 */
	public Pair<User, String> register(Map<String, String> parameters) throws IdentityException;
	
	/**
	 * @return the provider service's name
	 */
	public String getName();
}
