package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * Created on 28.02.17.
 */
public interface IdentityProvider {
	Pair<User, String> register(Map<String, String> parameters) throws IdentityException;
	String getName();
}
