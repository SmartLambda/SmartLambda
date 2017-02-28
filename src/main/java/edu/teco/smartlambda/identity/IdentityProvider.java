package edu.teco.smartlambda.identity;

import java.util.Map;
import java.util.Optional;

/**
 * Created on 28.02.17.
 */
public interface IdentityProvider {
	void register(Map<String, String> parameters) throws IdentityException;
	Optional<String> getName();
}
