package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;

import java.util.Optional;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * An abstract factory that creates lambda objects
 */
public abstract class LambdaFactory {
	
	/**
	 * Load a lambda by its owner and name from database
	 *
	 * @param owner lambda owner object
	 * @param name  lambda name
	 *
	 * @return an optional containing the saved lambda, if any exists with that name and owner
	 */
	protected final Optional<Lambda> fetchLambdaByOwnerAndName(final User owner, final String name) {
		final Lambda query = from(Lambda.class);
		where(query.getOwner()).eq(owner).and(query.getName()).eq(name);
		
		return select(query).get(Application.getInstance().getSessionFactory().getCurrentSession());
	}
	
	/**
	 * Get a decorated existing lambda by its owner and name
	 *
	 * @param owner user that owns the requested lambda
	 * @param name  name of the requested lambda
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
