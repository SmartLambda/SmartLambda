package edu.teco.smartlambda.identity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 28.02.17.
 */
public class IdentityProviderRegistry {
	private Map<String, IdentityProvider> providers;
	private static IdentityProviderRegistry instance = null;
	
	public static IdentityProviderRegistry getInstance() {
		if (instance == null) {
			instance = new IdentityProviderRegistry();
		}
		return  instance;
	}
	
	private IdentityProviderRegistry() {
		providers = new HashMap<String, IdentityProvider>();
		
		initialize();
	}
	
	private void initialize() {
		//Initialized the NullIdentityProvider
		providers.put("NullIdentityProvider", new NullIdentityProvider());
		//TODO for other IdentityProviders it could be bad to return always the same Instance of the Provider
	}
	
	public IdentityProvider getIdentityProviderByName(String name) {
		IdentityProvider identityProvider = providers.get(name);
		if (identityProvider == null) {
			throw new IdentityProviderNotFoundException();
		}
		return identityProvider;
	}
	
}
