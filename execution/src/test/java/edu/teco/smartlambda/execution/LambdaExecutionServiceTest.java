package edu.teco.smartlambda.execution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import edu.teco.smartlambda.shared.GlobalOptions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

/**
 * Unit tests for {@link LambdaExecutionService}
 */
public class LambdaExecutionServiceTest {
	
	// TODO add more test cases
	
	@Test
	public void testLambdaExecution() throws IOException {
		// the LambdaExectionService expects the lambda.jar existing
		Assume.assumeTrue(new File("lambda.jar").exists());
		
		// start the execution service asynchronously
		new Thread(LambdaExecutionService::main).start();
		
		final Gson             gson         = new GsonBuilder().create();
		final Socket           socket       = new Socket("localhost", GlobalOptions.PORT);
		final DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
		
		outputStream.writeUTF("{\"demoValue\": \"string\"}");
		outputStream.flush();
		
		final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
		
		final ExecutionReturnValue returnValue = gson.fromJson(inputStream.readUTF(), ExecutionReturnValue.class);
		
		Assert.assertFalse(returnValue.getException().isPresent());
		Assert.assertNotNull(returnValue.getReturnValue().get());
		
		Assert.assertEquals("{\"demoReturnValue\":\"success\"}", returnValue.getReturnValue().get());
	}
}