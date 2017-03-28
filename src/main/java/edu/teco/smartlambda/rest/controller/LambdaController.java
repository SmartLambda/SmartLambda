package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.rest.exception.LambdaNotFoundException;
import edu.teco.smartlambda.rest.exception.MissingSourceException;
import edu.teco.smartlambda.rest.exception.RuntimeNotFoundException;
import edu.teco.smartlambda.rest.exception.UserNotFoundException;
import edu.teco.smartlambda.runtime.Runtime;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import lombok.Data;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides REST calls related to lifecycle handling of lambdas.
 */
public class LambdaController {
	@Data
	private static class LambdaExecutionRequest {
		private Boolean    async;
		private ObjectNode parameters;
	}
	
	@Data
	private static class LambdaRequest {
		private Boolean async;
		private String  runtime;
		private byte[]  src;
	}
	
	@Data
	private static class LambdaResponse {
		private String  user;
		private String  name;
		private boolean async;
		private String  runtime;
	}
	
	@Data
	private static class StatisticsResponse {
		private long executions;
		private long averageExecutionTime;
		private long errors;
	}
	
	/**
	 * <code><b>PUT</b> /<i>:user</i>/lambda/<i>:name</i></code>
	 * <p>
	 * Creates a new lambda.
	 * </p>
	 * <p>
	 * The request must contain a new, unused lambda name. Responds with an empty JSON object on success.
	 * </p>
	 * <table>
	 * <caption><b>Body parameters</b></caption>
	 * <thead>
	 * <tr>
	 * <th>Name</th>
	 * <th>Type</th>
	 * <th>Description</th>
	 * <th>Required</th>
	 * </tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>async</td>
	 * <td>boolean</td>
	 * <td>Sets whether the lambda should be executed asynchronously per default</td>
	 * <td>default = false</td>
	 * </tr>
	 * <tr>
	 * <td>runtime</td>
	 * <td>enum("jre8")</td>
	 * <td>The runtime required for executing the lambda</td>
	 * <td>Yes</td>
	 * </tr>
	 * <tr>
	 * <td>src</td>
	 * <td>Base64-encoded byte stream</td>
	 * <td>Source code or binary container, depending on the runtime</td>
	 * <td>Yes</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to create new
	 *                                          lambdas
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner user is unknown
	 * @throws RuntimeNotFoundException         <b>400</b> Thrown when specified runtime is unknown
	 * @throws MissingSourceException           <b>400</b> Thrown when no source code or binary container was provided
	 */
	public static Object createLambda(final Request request, final Response response) throws IOException {
		final LambdaRequest  lambdaRequest = new ObjectMapper().readValue(request.body(), LambdaRequest.class);
		final AbstractLambda lambda        = LambdaFacade.getInstance().getFactory().createLambda();
		final Runtime        runtime       = RuntimeRegistry.getInstance().getRuntimeByName(lambdaRequest.getRuntime());
		
		if (runtime == null) throw new RuntimeNotFoundException(lambdaRequest.getRuntime());
		if (lambdaRequest.getSrc() == null || lambdaRequest.getSrc().length == 0) throw new MissingSourceException();
		
		lambda.setAsync(lambdaRequest.getAsync());
		lambda.setOwner(User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user"))));
		lambda.setName(request.params(":name"));
		lambda.setRuntime(runtime);
		lambda.deployBinary(lambdaRequest.getSrc());
		lambda.save();
		
		response.status(201);
		return new Object();
	}
	
	/**
	 * <code><b>PATCH</b> /<i>:user</i>/lambda/<i>:name</i></code>
	 * <p>
	 * Updates an existing lambda.
	 * </p>
	 * <p>
	 * The request must specify the name of an existing lambda. Responds with an empty JSON object on success.
	 * </p>
	 * <table>
	 * <caption><b>Body parameters</b></caption>
	 * <thead>
	 * <tr>
	 * <th>Name</th>
	 * <th>Type</th>
	 * <th>Description</th>
	 * <th>Required</th>
	 * </tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>async</td>
	 * <td>boolean</td>
	 * <td>Sets whether the lambda should be executed asynchronously per default</td>
	 * <td>No</td>
	 * </tr>
	 * <tr>
	 * <td>runtime</td>
	 * <td>enum("jre8")</td>
	 * <td>The runtime required for executing the lambda</td>
	 * <td>No</td>
	 * </tr>
	 * <tr>
	 * <td>src</td>
	 * <td>Base64-encoded byte stream</td>
	 * <td>Source code or binary container, depending on the runtime</td>
	 * <td>No</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to modify the
	 *                                          lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner user is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 * @throws RuntimeNotFoundException         <b>400</b> Thrown when specified runtime is unknown
	 * @throws MissingSourceException           <b>400</b> Thrown when source code or binary container is specified and not null but has
	 *                                          zero length
	 */
	public static Object updateLambda(final Request request, final Response response) throws IOException {
		final LambdaRequest lambdaRequest = new ObjectMapper().readValue(request.body(), LambdaRequest.class);
		final String        name          = request.params(":name");
		final User          user          = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name).orElseThrow(() -> new LambdaNotFoundException(name));
		
		if (lambdaRequest.getAsync() != null) lambda.setAsync(lambdaRequest.getAsync());
		if (lambdaRequest.getRuntime() != null) {
			final Runtime runtime = RuntimeRegistry.getInstance().getRuntimeByName(lambdaRequest.getRuntime());
			
			if (runtime == null) throw new RuntimeNotFoundException(lambdaRequest.getRuntime());
			
			lambda.setRuntime(runtime);
		}
		if (lambdaRequest.getSrc() != null) {
			if (lambdaRequest.getSrc().length == 0) throw new MissingSourceException();
			
			lambda.deployBinary(lambdaRequest.getSrc());
		}
		lambda.update();
		
		response.status(200);
		return new Object();
	}
	
	/**
	 * <code><b>GET</b> /<i>:user</i>/lambda/<i>:name</i></code>
	 * <p>
	 * Reads metadata about an existing lambda.
	 * </p>
	 * <p>
	 * The request must specify the name of an existing lambda. No body parameters are required.
	 * </p>
	 * <table>
	 * <caption><b>Response values</b></caption>
	 * <thead>
	 * <tr>
	 * <th>Name</th>
	 * <th>Type</th>
	 * <th>Description</th>
	 * </tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>user</td>
	 * <td>string</td>
	 * <td>Name of the lambda owner</td>
	 * </tr>
	 * <tr>
	 * <td>name</td>
	 * <td>string</td>
	 * <td>Lambda name</td>
	 * </tr>
	 * <tr>
	 * <td>async</td>
	 * <td>boolean</td>
	 * <td>True, if the lambda is executed asynchronously per default, false otherwise.</td>
	 * </tr>
	 * <tr>
	 * <td>runtime</td>
	 * <td>enum("jre8")</td>
	 * <td>The runtime used for executing the lambda.</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to read the
	 *                                          requested lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner user is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object readLambda(final Request request, final Response response) throws IOException {
		final LambdaResponse lambdaResponse = new LambdaResponse();
		final String         name           = request.params(":name");
		final User           user           = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name).orElseThrow(() -> new LambdaNotFoundException(name));
		
		lambdaResponse.setUser(lambda.getOwner().getName());
		lambdaResponse.setName(lambda.getName());
		lambdaResponse.setAsync(lambda.isAsync());
		lambdaResponse.setRuntime(lambda.getRuntime().getName());
		
		response.status(200);
		return lambdaResponse;
	}
	
	/**
	 * <code><b>DELETE</b> /<i>:user</i>/lambda/<i>:name</i></code>
	 * <p>
	 * Deletes an existing lambda.
	 * </p>
	 * <p>
	 * The request must specify the name of an existing lambda. No body parameters are required. Responds with an empty JSON object
	 * on success.
	 * </p>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is nor permitted to delete the
	 *                                          lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner user is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object deleteLambda(final Request request, final Response response) {
		final String name = request.params(":name");
		final User   user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		
		lambda.delete();
		
		response.status(200);
		return new Object();
	}
	
	/**
	 * <code><b>POST</b> /<i>:user</i>/lambda/<i>:name</i></code>
	 * <p>
	 * Executes a lambda.
	 * </p>
	 * <p>
	 * The request must specify the name of an existing lambda. The body contains the response generated by the lambda. Empty if no
	 * response is generated.
	 * </p>
	 * <table>
	 * <caption><b>Body parameters</b></caption>
	 * <thead>
	 * <tr>
	 * <th>Name</th>
	 * <th>Type</th>
	 * <th>Description</th>
	 * <th>Required</th>
	 * </tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>async</td>
	 * <td>boolean</td>
	 * <td>Whether to execute the lambda asynchronously</td>
	 * <td>default = default async value of lambda</td>
	 * </tr>
	 * <tr>
	 * <td>parameters</td>
	 * <td>object</td>
	 * <td>An arbitrary JSON object with parameters to pass to the lambda</td>
	 * <td>Yes</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to execute the
	 *                                          lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object executeLambda(final Request request, final Response response) throws IOException {
		final String                 name                   = request.params(":name");
		final LambdaExecutionRequest lambdaExecutionRequest = new ObjectMapper().readValue(request.body(), LambdaExecutionRequest.class);
		final User                   user                   =
				User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name).orElseThrow(() -> new LambdaNotFoundException(name));
		
		if ((lambda.isAsync() && lambdaExecutionRequest.async == null) || (lambdaExecutionRequest.async != null && lambdaExecutionRequest.async)) {
			lambda.executeAsync(lambdaExecutionRequest.getParameters() != null ? lambdaExecutionRequest.getParameters().toString() : "");
			response.status(202);
			return "";
		} else {
			final ExecutionReturnValue executionReturnValue = lambda.executeSync(
					lambdaExecutionRequest.getParameters() != null ? lambdaExecutionRequest.getParameters().toString() : "").getExecutionReturnValue();
			if (executionReturnValue.isException()) {
				response.status(502);
				return "";
			} else {
				response.status(200);
				return executionReturnValue.getReturnValue().orElse("");
			}
		}
	}
	
	/**
	 * <code><b>GET</b> /<i>:user</i>/lambdas</code>
	 * <p>
	 * Gets a list of lambdas owned by the specified user that are visible to the authenticated user. No body parameters are required
	 * . The response contains an array of objects in the style of the table below. User must be authenticated with their primary key.
	 * </p>
	 * <table>
	 * <caption><b>Response object values</b></caption>
	 * <thead>
	 * <tr>
	 * <th>Name</th>
	 * <th>Type</th>
	 * <th>Description</th>
	 * </tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>user</td>
	 * <td>string</td>
	 * <td>Name of the lambda owner</td>
	 * </tr>
	 * <tr>
	 * <td>name</td>
	 * <td>string</td>
	 * <td>Lambda name</td>
	 * </tr>
	 * <tr>
	 * <td>async</td>
	 * <td>boolean</td>
	 * <td>True, if the lambda is executed asynchronously per default, false otherwise.</td>
	 * </tr>
	 * <tr>
	 * <td>runtime</td>
	 * <td>enum("jre8")</td>
	 * <td>The runtime used for executing the lambda.</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when user is not authenticated with their primary key
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner user is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object getLambdaList(final Request request, final Response response) {
		final List<LambdaResponse> lambdas = new LinkedList<>();
		
		for (final AbstractLambda lambda : User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user"))).getVisibleLambdas()) {
			final LambdaResponse lambdaResponse = new LambdaResponse();
			lambdaResponse.setName(lambda.getName());
			lambdaResponse.setUser(lambda.getOwner().getName());
			lambdaResponse.setRuntime(lambda.getRuntime().getName());
			lambdaResponse.setAsync(lambda.isAsync());
			
			lambdas.add(lambdaResponse);
		}
		
		response.status(200);
		return lambdas;
	}
	
	/**
	 * <code><b>GET</b> /<i>:user</i>/lambda/<i>:name</i>/statistics</code>
	 * <p>
	 * Reads statistical information about past executions of the lambda. No body parameters are required.
	 * </p>
	 * <table>
	 * <caption><b>Response values</b></caption>
	 * <thead>
	 * <tr>
	 * <th>Name</th>
	 * <th>Type</th>
	 * <th>Description</th>
	 * </tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>executions</td>
	 * <td>integer</td>
	 * <td>Number of times the lambda was executed</td>
	 * </tr>
	 * <tr>
	 * <td>errors</td>
	 * <td>integer</td>
	 * <td>Number of times the lambda threw an error on execution</td>
	 * </tr>
	 * <tr>
	 * <td>averageExecutionTime</td>
	 * <td>float</td>
	 * <td>Average duration of past executions</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to read statistics
	 *                                          about the requested lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner user is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object getStatistics(final Request request, final Response response) {
		final StatisticsResponse statisticsResponse = new StatisticsResponse();
		final User               user               = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		long executions         = 0;
		long totalExecutionTime = 0;
		long errors             = 0;
		
		for (final MonitoringEvent event : LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, request.params(":name")).orElseThrow(() -> new LambdaNotFoundException(request.params(":name"))).getMonitoringEvents()) {
			executions++;
			if (event.getError() != null) errors++;
			
			totalExecutionTime += event.getDuration();
		}
		
		statisticsResponse.setExecutions(executions);
		statisticsResponse.setErrors(errors);
		statisticsResponse.setAverageExecutionTime(totalExecutionTime / executions);
		
		response.status(200);
		return statisticsResponse;
	}
}
