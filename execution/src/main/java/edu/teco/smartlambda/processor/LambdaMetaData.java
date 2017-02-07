package edu.teco.smartlambda.processor;

import lombok.Data;

/**
 * A model class for the meta data saved to the meta file (named @value LambdaExecutionService#LAMBDA_META_DATA_FILE)
 */
@Data
public class LambdaMetaData {
	private final String lambdaClassName;
	private final String lambdaMethodName;
	
	private final boolean hasParameter;
	private final boolean hasReturnValue;
	
	private final String lambdaParameterClassName;
	private final String lambdaReturnValueClassName;
}
