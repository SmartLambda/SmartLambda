package edu.teco.smartlambda.execution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Cleanup;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TODO javadoc
 */
public class LambdaExecutionService {
	
	public static final String LAMBDA_META_DATA_FILE = "META-INF/lambda.json";
	
	private static final int PORT = 31337;
	
	public static void main(final String... args) throws IOException {
		@Cleanup final ServerSocket     socket       = new ServerSocket(PORT);
		@Cleanup final Socket           clientSocket = socket.accept();
		@Cleanup final DataInputStream  input        = new DataInputStream(clientSocket.getInputStream());
		@Cleanup final DataOutputStream output       = new DataOutputStream(clientSocket.getOutputStream());
		
		final String jsonParameter = input.readUTF();
		final Gson   gson          = new GsonBuilder().create();
		
		// TODO load lambda archive into classpath
		
		// TODO deserialize parameter
		
		// TODO start and execute lambda with parameter and gather return value
		
		// TODO serialize return value
		final String returnValue = "";
		
		output.flush();
		output.writeUTF(returnValue);
		output.flush();
	}
}
