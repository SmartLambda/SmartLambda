package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.authentication.entities.User;

import java.util.Optional;

/**
 * An abstract factory that creates lambda objects
 */
public abstract class LambdaFactory {
	
	/**
	 * Get an existing lambda by its owner and name
	 *
	 * @param owner user that owns the requested lambda
	 * @param name name of the requested lambda
	 *
	 * @return a previously created lambda that was saved in the database
	 */
	public abstract Optional<AbstractLambda> getLambdaByOwnerAndName(final User owner, final String name);
	
	/**
	 * Create a new decorated lambda instance
	 *
	 * @return a decorated lambda instance
	 */
	public abstract AbstractLambda createLambda();
	
	/**
	 * Decorates a lambda
	 *
	 * @return a decorated lambda instance
	 */
	public abstract AbstractLambda decorate(final Lambda lambda);
}
