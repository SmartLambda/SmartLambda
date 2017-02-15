package edu.teco.smartlambda.runtime;

/**
 * The runtime environment of a lambda
 */
public interface Runtime {
	
	/**
	 * @return //// FIXME: 2/15/17
	 */
	public String getCommand();
	
	/**
	 * @return the name of the runtime
	 */
	public String getName();
	
	/**
	 * @return the name of the runtime specific binary that shall be executed in the container and will receive the lambda call context,
	 * execute it and return the return value
	 */
	public String getBinaryName();
}
