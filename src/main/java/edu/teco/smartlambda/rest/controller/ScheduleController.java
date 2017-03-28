package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.rest.exception.EventNotFoundException;
import edu.teco.smartlambda.rest.exception.LambdaNotFoundException;
import edu.teco.smartlambda.rest.exception.UserNotFoundException;
import edu.teco.smartlambda.schedule.Event;
import lombok.Data;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides REST calls related to lifecycle handling of scheduled events.
 */
public class ScheduleController {
	@Data
	private static class ScheduleRequest {
		private String     calendar;
		private ObjectNode parameters;
	}
	
	@Data
	private static class ScheduleResponse {
		private String name;
		private String calendar;
		private String parameters;
	}
	
	/**
	 * <code><b>PUT</b> /<i>:user</i>/lambda/<i>:name</i>/schedule/<i>:event-name</i></code>
	 * <p>
	 * Creates a new scheduled event. The request must contain the name of an existing lambda. Responds with an empty JSON object on
	 * success.
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
	 * <td>calendar</td>
	 * <td>string</td>
	 * <td>A cron-style timestamp</td>
	 * <td>Yes</td>
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
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to schedule events
	 *                                          on the lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object createSchedule(final Request request, final Response response) throws IOException {
		final ScheduleRequest scheduleRequest = new ObjectMapper().readValue(request.body(), ScheduleRequest.class);
		final User            user            =
				User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params("user")));
		final String          name            = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		final Event event = new Event();
		event.setName(request.params(":event-name"));
		event.setCronExpression(scheduleRequest.getCalendar());
		event.setKey(AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
		event.setParameters(scheduleRequest.getParameters().toString());
		lambda.schedule(event);
		
		response.status(201);
		return new Object();
	}
	
	/**
	 * <code><b>PATCH</b> /<i>:user</i>/lambda/<i>:name</i>/schedule/<i>:event-name</i></code>
	 * <p>
	 * Updates an existing scheduled event. The request must contain the name of an existing lambda and schedule. Responds with an empty
	 * JSON object on success.
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
	 * <td>calendar</td>
	 * <td>string</td>
	 * <td>A cron-style timestamp</td>
	 * <td>No</td>
	 * </tr>
	 * <tr>
	 * <td>parameters</td>
	 * <td>object</td>
	 * <td>An arbitrary JSON object with parameters to pass to the lambda</td>
	 * <td>No</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to schedule events
	 *                                          on the lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object updateSchedule(final Request request, final Response response) throws IOException {
		final ScheduleRequest scheduleRequest = new ObjectMapper().readValue(request.body(), ScheduleRequest.class);
		final User            user            =
				User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final String name = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		final Event event = lambda.getScheduledEvent(request.params(":event-name")).orElseThrow(() -> new EventNotFoundException(request.params(":event-name")));
		
		if (scheduleRequest.getCalendar() != null) event.setCronExpression(scheduleRequest.getCalendar());
		
		if (scheduleRequest.getParameters() != null) event.setParameters(scheduleRequest.getParameters().toString());
		
		lambda.schedule(event);
		response.status(200);
		return new Object();
	}
	
	/**
	 * <code><b>GET</b> /<i>:user</i>/lambda/<i>:name</i>/schedule/<i>:event-name</i></code>
	 * <p>
	 * Reads information about an existing scheduled event. No body parameters are required. The request must contain the name of an
	 * existing lambda and schedule.
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
	 * <td>calendar</td>
	 * <td>string</td>
	 * <td>Execution timestamp</td>
	 * </tr>
	 * <tr>
	 * <td>parameters</td>
	 * <td>object</td>
	 * <td>JSON parameter object passed to the lambda</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to read scheduled
	 *                                          events of the lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object readSchedule(final Request request, final Response response) {
		final User   user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final String name = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		final Event event = lambda.getScheduledEvent(request.params(":event-name")).orElseThrow(() -> new EventNotFoundException(request.params(":event-name")));
		final ScheduleResponse scheduleResponse = new ScheduleResponse();
		scheduleResponse.setName(event.getName());
		scheduleResponse.setParameters(event.getParameters());
		scheduleResponse.setCalendar(event.getCronExpression());
		
		response.status(200);
		return scheduleResponse;
	}
	
	/**
	 * <code><b>DELETE</b> /<i>:user</i>/lambda/<i>:name</i>/schedule/<i>:event-name</i></code>
	 * <p>
	 * Deletes an existing scheduled event. The request must contain the name of an existing lambda and schedule. Responds with an empty
	 * JSON object on success.
	 * </p>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to delete scheduled
	 *                                          events of the lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object deleteSchedule(final Request request, final Response response) {
		final User   user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final String name = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		final Event event = lambda.getScheduledEvent(request.params(":event-name"))
				.orElseThrow(() -> new EventNotFoundException(request.params(":event-name")));
		
		event.delete();
		
		response.status(200);
		return new Object();
	}
	
	/**
	 * <code><b>GET</b> /<i>:user</i>/lambda/<i>:name</i>/schedules</code>
	 * <p>
	 * Lists existing scheduled events of a lambda. The request must contain the name of an existing lambda.
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
	 * <td>name</td>
	 * <td>string</td>
	 * <td>Scheduled event name</td>
	 * </tr>
	 * <tr>
	 * <td>calendar</td>
	 * <td>string</td>
	 * <td>A cron-style timestamp</td>
	 * <td>No</td>
	 * </tr>
	 * <tr>
	 * <td>parameters</td>
	 * <td>object</td>
	 * <td>An arbitrary JSON object with parameters to pass to the lambda</td>
	 * <td>No</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to read scheduled
	 *                                          events of the lambda
	 * @throws UserNotFoundException            <b>404</b> Thrown when target lambda owner is unknown
	 * @throws LambdaNotFoundException          <b>404</b> Thrown when target lambda is unknown
	 */
	public static Object getScheduledEvents(final Request request, final Response response) {
		final User   user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final String name = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name).orElseThrow(() -> new LambdaNotFoundException(name));
		final List<Event>            events         = lambda.getScheduledEvents();
		final List<ScheduleResponse> responseEvents = new LinkedList<>();
		
		for (final Event event : events) {
			final ScheduleResponse scheduleResponse = new ScheduleResponse();
			scheduleResponse.setName(event.getName());
			scheduleResponse.setCalendar(event.getCronExpression());
			scheduleResponse.setParameters(event.getParameters());
			
			responseEvents.add(scheduleResponse);
		}
		
		response.status(200);
		return responseEvents;
	}
}
