package edu.teco.smartlambda.execution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.teco.smartlambda.container.ExecutionReturnValue;
import org.junit.Assert;
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
		assert new File("lambda.jar").exists() : "the test lambda archive is missing";
		
		// start the execution service asynchronously
		new Thread(LambdaExecutionService::main).start();
		
		final Gson             gson         = new GsonBuilder().create();
		final Socket           socket       = new Socket("localhost", 31337);
		final DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
		
		outputStream.writeUTF("{\"demoValue\": \"string\"}");
		outputStream.flush();
		
		final DataInputStream inputStream = new DataInputStream(socket.getInputStream());
		
		final ExecutionReturnValue returnValue = gson.fromJson(inputStream.readUTF(), ExecutionReturnValue.class);
		
		Assert.assertNull(returnValue.getException());
		Assert.assertNotNull(returnValue.getReturnValue());
		
		Assert.assertEquals("{\"demoReturnValue\":\"success\"}", returnValue.getReturnValue());
	}
}