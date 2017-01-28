package edu.teco.smartlambda.demo.test;

import edu.teco.smartlambda.execution.LambdaParameter;
import lombok.Data;

/**
 * Parameter class for the demonstration lambda
 */
@Data
public class DemoParameter implements LambdaParameter {
	private final String demoValue;
}
