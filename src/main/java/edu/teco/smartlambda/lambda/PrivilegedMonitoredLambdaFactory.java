package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.authentication.entities.User;

import java.util.Optional;

public class PrivilegedMonitoredLambdaFactory extends LambdaFactory {
	@Override
	public Optional<AbstractLambda> getLambdaByOwnerAndName(final User owner, final String name) {
		return this.fetchLambdaByOwnerAndName(owner, name).map(this::decorate);
	}
	
	/**
	 * Create a new lambda model instance with default decoration
	 *
	 * @return an empty lambda object decorated with authentication and monitoring
	 */
	@Override
	public AbstractLambda createLambda() {
		return this.decorate(new Lambda());
	}
	
	@Override
	public AbstractLambda decorate(final Lambda lambda) {
		return new PermissionDecorator(new MonitoringDecorator(lambda));
	}
}
