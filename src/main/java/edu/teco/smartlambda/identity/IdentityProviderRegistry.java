package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.configuration.ConfigurationService;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created on 28.02.17.
 */
public class IdentityProviderRegistry {
	private final  Map<String, IdentityProvider> providers = new HashMap<>();
	private static IdentityProviderRegistry      instance  = null;
	
	public static IdentityProviderRegistry getInstance() {
		if (instance == null) {
			instance = new IdentityProviderRegistry();
		}
		return instance;
	}
	
	private IdentityProviderRegistry() {
		final List<String> providers =
				ConfigurationService.getInstance().getConfiguration().getList(String.class, "identityProviders.identityProvider");
		
		if (providers == null) {
			LoggerFactory.getLogger(IdentityProviderRegistry.class).error("No identity providers defined");
			return;
		}
		
		for (final String provider : providers) {
			try {
				final IdentityProvider providerInstance = (IdentityProvider) Class.forName(provider).getConstructor().newInstance();
				this.providers.put(providerInstance.getName(), providerInstance);
				LoggerFactory.getLogger(IdentityProviderRegistry.class).info("Loaded identity provider " + provider);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				LoggerFactory.getLogger(IdentityProviderRegistry.class).error("Exception while loading identity provider");
				e.printStackTrace();
			} catch (final ClassNotFoundException e) {
				LoggerFactory.getLogger(IdentityProviderRegistry.class).error("Identity provider " + provider + " not found");
			}
		}
	}
	
	public Optional<IdentityProvider> getIdentityProviderByName(final String name) {
		return Optional.ofNullable(this.providers.get(name));
	}
}
