package edu.teco.smartlambda.lambda;

/**
 * Decorates lambdas with authenticagtion and aborts the lambda call, if authentication fails
 */
//// FIXME: 2/15/17 
public class PermissionDecorator extends LambdaDecorator {
	
	public PermissionDecorator(final AbstractLambda lambda) {
		super(lambda);
	}
}
