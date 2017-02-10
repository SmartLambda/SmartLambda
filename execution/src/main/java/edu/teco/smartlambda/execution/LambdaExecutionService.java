package edu.teco.smartlambda.execution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.teco.smartlambda.processor.LambdaMetaData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * The application that runs inside a virtual container and shall receive the lambda parameter, execute the lambda and return the return
 * value
 */
public class LambdaExecutionService {
	
	/**
	 * the meta file location inside the lambda archive
	 */
	public static final String LAMBDA_META_DATA_FILE = "META-INF/lambda.json";
	
	/**
	 * the location of the lambda archive that is loaded
	 */
	private static final String LAMBDA_ARCHIVE_LOCATION = "lambda.jar";
	
	/**
	 * the port where parameters are received
	 */
	private static final int PORT = 31337;
	
	/**
	 * Main function of the lambda executor service that executes the lambda archive inside a container
	 *
	 * @param args ignored command line parameters
	 */
	public static void main(final String... args) {
		
		final ServerSocket     socket;
		final Socket           clientSocket;
		final DataInputStream  input;
		final DataOutputStream output;
		
		final Gson gson = new GsonBuilder().create();
		
		// the reported return value
		ExecutionReturnValue executionReturnValue = new ExecutionReturnValue(null, null);
		
		// initialize socket
		try {
			socket = new ServerSocket(PORT);
			clientSocket = socket.accept();
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			// fatal. Cannot be reported
			e.printStackTrace();
			return;
		}
		
		try {
			// initialize class loader
			final URLClassLoader classLoader;
			try {
				classLoader = new URLClassLoader(new URL[] {new File(LAMBDA_ARCHIVE_LOCATION).toURI().toURL()}, LambdaExecutionService
						.class.getClassLoader());
			} catch (MalformedURLException e) {
				assert false;
				return;
			}
			
			// receive serialized parameter
			final String         jsonParameter = input.readUTF();
			final LambdaMetaData metaData;
			
			// acquire meta data object
			metaData = readMetaData(gson, classLoader);
			
			final Class<?> lambdaMainClass;
			final Class<?> lambdaParameterClass;
			final Method   lambdaFunction;
			try {
				lambdaMainClass = classLoader.loadClass(metaData.getLambdaClassName());
				lambdaParameterClass = metaData.isHasParameter() ? classLoader.loadClass(metaData.getLambdaParameterClassName()) : null;
				
				lambdaFunction = lambdaMainClass.getDeclaredMethod(metaData.getLambdaMethodName(), lambdaParameterClass);
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				e.printStackTrace();
				executionReturnValue = new ExecutionReturnValue(null, new InvalidLambdaDefinitionException("Invalid lambda meta file: " +
				                                                                                           e.getMessage()));
				return;
			}
			
			assert (lambdaParameterClass != null) == metaData.isHasParameter();
			final Object lambdaParameter = lambdaParameterClass != null ? gson.fromJson(jsonParameter, lambdaParameterClass) : null;
			
			final String serializedReturnValue;
			try {
				Object returnValue = lambdaFunction.invoke(lambdaMainClass.getConstructor().newInstance(), lambdaParameter);
				executionReturnValue = new ExecutionReturnValue(gson.toJson(returnValue), null);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
				executionReturnValue = new ExecutionReturnValue(null, new InvalidLambdaDefinitionException("No accessible default " +
				                                                                                           "constructor in lambda class"));
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				executionReturnValue = new ExecutionReturnValue(null, new InvalidLambdaDefinitionException("Could not invoke lambda " +
				                                                                                           "function: " + e.getMessage()));
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				executionReturnValue = new ExecutionReturnValue(null, e.getCause());
			}
		} catch (IOException e) {
			e.printStackTrace();
			executionReturnValue = new ExecutionReturnValue(null, new Exception("Internal Server Error"));
		} finally {
			try {
				output.flush();
				output.writeUTF(new GsonBuilder().create().toJson(executionReturnValue));
				output.flush();
				
				output.close();
				input.close();
				clientSocket.close();
				socket.close();
			} catch (IOException e) {
				// fatal. cannot be reported
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Read the lambda meta data file and deserialize it
	 *
	 * @param classLoader the classloader containing the meta file
	 *
	 * @return the deserialized meta data object
	 *
	 * @throws IOException on stream fail
	 */
	private static LambdaMetaData readMetaData(final Gson gson, final URLClassLoader classLoader) throws IOException {
		return gson.fromJson(new InputStreamReader(classLoader.findResource(LAMBDA_META_DATA_FILE).openStream()), LambdaMetaData.class);
	}
}
