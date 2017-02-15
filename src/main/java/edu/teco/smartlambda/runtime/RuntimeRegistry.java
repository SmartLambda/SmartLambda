package edu.teco.smartlambda.runtime;

import java.util.HashMap;
import java.util.Map;

/**
 * A singleton registry for runtimes
 */
public class RuntimeRegistry {
	
	private static RuntimeRegistry instance;
	
	private final Map<String, Runtime> runtimes = new HashMap<>();
	
	private RuntimeRegistry() {
		// TODO load registry instances from config
	}
	
	public static RuntimeRegistry getInstance() {
		if (instance == null) instance = new RuntimeRegistry();
		return instance;
	}
}
