package edu.teco.smartlambda.lambda;

public class PrivilegedMonitoredLambdaFactory extends LambdaFactory {
	
	//// FIXME: 2/17/17 
	@Override
	public AbstractLambda getLambdaByName(final String name) {
		return null;
	}
	
	/**
	 * Create a new lambda model instance with default decoration
	 *
	 * @return an empty lambda object decorated with authentication and monitoring
	 */
	@Override
	public AbstractLambda createLambda() {
		return new PermissionDecorator(new MonitoringDecorator(new Lambda()));
	}
}
