package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;

import java.util.Optional;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

public class PrivilegedMonitoredLambdaFactory extends LambdaFactory {
	@Override
	public Optional<AbstractLambda> getLambdaByOwnerAndName(final User owner, final String name) {
		final Lambda query = from(Lambda.class);
		where(query.getOwner()).eq(owner).and(query.getName()).eq(name);
		
		return Optional.ofNullable(select(query).get(Application.getInstance().getSessionFactory().getCurrentSession()));
		// TODO decorate
	}
	
	/**
	 * Create a new lambda model instance with default decoration
	 *
	 * @return an empty lambda object decorated with authentication and monitoring
	 */
	@Override
	public AbstractLambda createLambda() {
		return new Lambda();
		// TODO decorate
		//return new PermissionDecorator(new MonitoringDecorator(new Lambda()));
	}
}
