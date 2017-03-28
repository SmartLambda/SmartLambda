package edu.teco.smartlambda.lambda;

import lombok.Getter;

/**
 * Provides access to the lambda system by providing a getter for a factory implementation that creates {@link AbstractLambda} objects
 */
public class LambdaFacade {
	
	private static LambdaFacade instance;
	
	@Getter
	private final LambdaFactory factory;
	
	/**
	 * This class is a singleton and therefore handles instantiation itself
	 */
	private LambdaFacade() {
		// this can be interchanged when other factories shall be supported
		this.factory = new PrivilegedMonitoredLambdaFactory();
	}
	
	/**
	 * @return the singleton instance of this class
	 */
	public static LambdaFacade getInstance() {
		if (instance == null) instance = new LambdaFacade();
		return instance;
	}
}
