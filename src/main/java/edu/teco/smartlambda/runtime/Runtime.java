package edu.teco.smartlambda.runtime;

/**
 * The runtime environment of a lambda
 */
public interface Runtime {
	
	/**
	 * @return the runtime specific command that executes a lambda within a started container
	 */
	public String getCommand();
	
	/**
	 * @return the name of the runtime
	 */
	public String getName();
	
	/**
	 * @return the name of the lambda binary file inside the container. It is the same for all lambdas that are of the same runtime
	 */
	public String getBinaryName();
}
