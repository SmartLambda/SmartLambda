package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.configuration.ConfigurationService;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A singleton registry for runtimes
 */
public class RuntimeRegistry {
	
	private static RuntimeRegistry instance;
	
	private final Map<String, Runtime> runtimes = new HashMap<>();
	
	private RuntimeRegistry() {
		final List<String> runtimes = ConfigurationService.getInstance().getConfiguration().getList(String.class, "runtimes.runtime");
		
		if (runtimes == null) {
			LoggerFactory.getLogger(RuntimeRegistry.class).error("No runtimes defined");
			return;
		}
		
		for (String runtime : runtimes) {
			try {
				final Runtime runtimeInstance = (Runtime) Class.forName(runtime).getConstructor().newInstance();
				this.runtimes.put(runtime, runtimeInstance);
				LoggerFactory.getLogger(RuntimeRegistry.class).info("Loaded runtime " + runtime);
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				LoggerFactory.getLogger(RuntimeRegistry.class).error("Exception while loading runtime");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				LoggerFactory.getLogger(RuntimeRegistry.class).error("Runtime " + runtime + " not found");
			}
		}
	}
	
	public static RuntimeRegistry getInstance() {
		if (instance == null) instance = new RuntimeRegistry();
		return instance;
	}
	
	public Runtime getRuntimeByName(final String name) {
		return runtimes.get(name);
	}
}
