package edu.teco.smartlambda.lambda;

/**
 * An abstract factory that creates lambda objects
 */
public abstract class LambdaFactory {
	
	/**
	 * @param name name of the requested lambda
	 *
	 * @return a previously created lambda that was saved in the database
	 */
	public abstract AbstractLambda getLambdaByName(final String name);
	
	/**
	 * Create a new decorated lambda instance
	 *
	 * @return a decorated lambda instance
	 */
	public abstract AbstractLambda createLambda();
}
