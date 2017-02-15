package edu.teco.smartlambda.lambda;

/**
 * Decorates lambdas with calls to the monitoring service
 */
//// FIXME: 2/15/17 
public class MonitoringDecorator extends LambdaDecorator {
	
	public MonitoringDecorator(final AbstractLambda lambda) {
		super(lambda);
	}
}
