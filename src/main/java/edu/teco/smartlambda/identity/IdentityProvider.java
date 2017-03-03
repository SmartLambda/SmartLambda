package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;

import java.util.Map;

/**
 * Created on 28.02.17.
 */
public interface IdentityProvider {
	User register(Map<String, String> parameters) throws IdentityException;
	String getName();
}
