package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.rest.exception.LambdaNotFoundException;
import edu.teco.smartlambda.rest.exception.UserNotFoundException;
import edu.teco.smartlambda.schedule.Event;
import lombok.Data;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class ScheduleController {
	@Data
	private static class ScheduleRequest {
		private String     calendar;
		private ObjectNode parameters;
	}
	
	@Data
	private static class ScheduleResponse {
		private String calendar;
		private String parameters;
	}
	
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
		return null;
	}
	
	public static Object updateSchedule(final Request request, final Response response) throws IOException {
		final ScheduleRequest scheduleRequest = new ObjectMapper().readValue(request.body(), ScheduleRequest.class);
		final User            user            =
				User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final String          name            = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		final Event event = lambda.getScheduledEvent(request.params(":event-name"));
		
		if (scheduleRequest.getCalendar() != null) event.setCronExpression(scheduleRequest.getCalendar());
		
		if (scheduleRequest.getParameters() != null) event.setParameters(scheduleRequest.getParameters().toString());
		
		response.status(200);
		return null;
	}
	
	public static Object readSchedule(final Request request, final Response response) {
		final User   user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final String name = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		final Event            event            = lambda.getScheduledEvent(request.params(":event-name"));
		final ScheduleResponse scheduleResponse = new ScheduleResponse();
		scheduleResponse.setParameters(event.getParameters());
		scheduleResponse.setCalendar(event.getCronExpression());
		
		response.status(200);
		return scheduleResponse;
	}
	
	public static Object deleteSchedule(final Request request, final Response response) {
		final User   user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final String name = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		final Event event = lambda.getScheduledEvent(request.params(":event-name"));
		
		event.delete();
		
		response.status(200);
		return null;
	}
	
	public static Object getScheduledEvents(final Request request, final Response response) {
		final User   user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final String name = request.params(":name");
		final AbstractLambda lambda = LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(user, name)
				.orElseThrow(() -> new LambdaNotFoundException(name));
		final List<Event>            events         = lambda.getScheduledEvents();
		final List<ScheduleResponse> responseEvents = new LinkedList<>();
		
		for (final Event event : events) {
			final ScheduleResponse scheduleResponse = new ScheduleResponse();
			scheduleResponse.setCalendar(event.getCronExpression());
			scheduleResponse.setParameters(event.getParameters());
			
			responseEvents.add(scheduleResponse);
		}
		
		response.status(200);
		return responseEvents;
	}
}
