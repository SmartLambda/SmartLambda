package edu.teco.smartlambda.runtime;

import edu.teco.smartlambda.shared.ExecutionReturnValue;
import lombok.Data;

@Data
public class ExecutionResult {
	private ExecutionReturnValue executionReturnValue;
	private long                 consumedCPUTime;
}
