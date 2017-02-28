package edu.teco.smartlambda.identity;

import java.util.Map;
import java.util.Optional;

/**
 * Created on 28.02.17.
 */
public class NullIdentityProvider implements IdentityProvider{
	private String name = null;
	
	NullIdentityProvider() {
		
	}
	
	@Override
	public void register(final Map<String, String> parameters) throws IdentityException {
		name = parameters.get("name");
		if (name == null) {
			throw new IdentitySyntaxException();
		};
	}
	
	@Override
	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}
}
