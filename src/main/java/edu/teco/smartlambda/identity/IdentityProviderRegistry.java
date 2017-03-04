package edu.teco.smartlambda.identity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 28.02.17.
 */
public class IdentityProviderRegistry {
	private Map<String, Class<? extends IdentityProvider>> providers;
	private static IdentityProviderRegistry instance = null;
	
	public static IdentityProviderRegistry getInstance() {
		if (instance == null) {
			instance = new IdentityProviderRegistry();
		}
		return  instance;
	}
	
	private IdentityProviderRegistry() {
		providers = new HashMap<>();
		
		initialize();
	}
	
	private void initialize() {
		//Initialized the NullIdentityProvider
		initializeIdentityProviderWithStandardName(NullIdentityProvider.class);
	}
	
	private void initializeIdentityProviderWithStandardName(Class<? extends IdentityProvider> ipClass) {
		providers.put(ipClass.getName(), ipClass);
		System.out.println(ipClass.getName());
	}
	
	public IdentityProvider getIdentityProviderByName(String name) {
		IdentityProvider identityProvider = null;
		if (providers.get(name) == null) {
			throw new IdentityProviderNotFoundException();
		}
		try {
			identityProvider = providers.get(name).newInstance();
		} catch (InstantiationException e) {
			throw new IdentityException(e.getLocalizedMessage());
		} catch (IllegalAccessException e) {
			throw new IdentityException(e.getLocalizedMessage());
		}
		return identityProvider;
	}
	
}
