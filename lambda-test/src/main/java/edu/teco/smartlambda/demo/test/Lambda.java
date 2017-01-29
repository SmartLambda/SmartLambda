package edu.teco.smartlambda.demo.test;

import edu.teco.smartlambda.execution.LambdaFunction;
import edu.teco.smartlambda.execution.LambdaParameter;
import edu.teco.smartlambda.execution.LambdaReturnValue;

/**
 * A demo Lambda function for test cases
 */
public class Lambda {
	
	/**
	 * A demonstration lambda function taking the expected parameter and returning the expected value
	 *
	 * @param demoParameter an instance of a {@link LambdaParameter} implementation
	 *
	 * @return an instance of a {@link LambdaReturnValue} implementation
	 */
	@LambdaFunction
	public DemoReturnValue demoLambda(final DemoParameter demoParameter) {
		return new DemoReturnValue("success");
	}
}
