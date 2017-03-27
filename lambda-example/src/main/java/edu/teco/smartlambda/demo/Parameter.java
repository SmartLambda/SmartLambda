package edu.teco.smartlambda.demo;

import edu.teco.smartlambda.execution.LambdaParameter;
import lombok.Data;

/**
 *
 */
@Data
public class Parameter implements LambdaParameter {
	private final String URL;
}
