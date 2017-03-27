package edu.teco.smartlambda.demo;

import edu.teco.smartlambda.execution.LambdaReturnValue;
import lombok.Data;

/**
 *
 */
@Data
public class AsciiImage implements LambdaReturnValue {
	private final String image;
}
