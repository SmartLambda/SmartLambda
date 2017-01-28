package edu.teco.smartlambda.demo.test;

import edu.teco.smartlambda.execution.LambdaReturnValue;
import lombok.Data;

/**
 * A return value for the demonstration lambda
 */
@Data
public class DemoReturnValue implements LambdaReturnValue {
	private final String demoReturnValue;
}
