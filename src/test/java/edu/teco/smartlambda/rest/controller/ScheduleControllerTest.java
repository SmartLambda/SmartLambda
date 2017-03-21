package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.lambda.LambdaFactory;
import edu.teco.smartlambda.schedule.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import spark.Request;
import spark.Response;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LambdaFacade.class, User.class, AuthenticationService.class})
public class ScheduleControllerTest {
	private static final String TEST_USER_NAME       = "TestUser";
	private static final String TEST_LAMBDA_NAME     = "TestLambda";
	private static final String TEST_SCHEDULE_NAME   = "TestSchedule";
	private static final String TEST_PARAMETER_NAME  = "TestParameter";
	private static final String TEST_PARAMETER_VALUE = "TestParameterValue";
	private static final String TEST_CRON_EXPRESSION = "30 01 * * *";
	private AbstractLambda testLambda;
	private Key            testKey;
	
	@RequiredArgsConstructor
	private static class ScheduleRequest {
		private final String     calendar;
		private final JsonObject parameters;
	}
	
	@Data
	@AllArgsConstructor
	private static class ScheduleResponse {
		private final String     name;
		private final String     calendar;
		private final ObjectNode parameters;
	}
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(LambdaFacade.class);
		PowerMockito.mockStatic(User.class);
		PowerMockito.mockStatic(AuthenticationService.class);
		
		this.testLambda = mock(AbstractLambda.class);
		final User user = mock(User.class);
		
		final LambdaFactory lambdaFactory = mock(LambdaFactory.class);
		final LambdaFacade  lambdaFacade  = mock(LambdaFacade.class);
		
		when(User.getByName(TEST_USER_NAME)).thenReturn(Optional.of(user));
		when(LambdaFacade.getInstance()).thenReturn(lambdaFacade);
		when(lambdaFacade.getFactory()).thenReturn(lambdaFactory);
		when(lambdaFactory.getLambdaByOwnerAndName(user, TEST_LAMBDA_NAME)).thenReturn(Optional.of(this.testLambda));
		
		this.testKey = mock(Key.class);
		
		final AuthenticationService authenticationService = mock(AuthenticationService.class);
		when(AuthenticationService.getInstance()).thenReturn(authenticationService);
		when(authenticationService.getAuthenticatedKey()).thenReturn(Optional.of(this.testKey));
	}
	
	@Test
	public void createSchedule() throws Exception {
		final Request    request    = mock(Request.class);
		final Response   response   = mock(Response.class);
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(TEST_PARAMETER_NAME, TEST_PARAMETER_VALUE);
		
		when(request.params(":user")).thenReturn(TEST_USER_NAME);
		when(request.params(":name")).thenReturn(TEST_LAMBDA_NAME);
		when(request.params(":event-name")).thenReturn(TEST_SCHEDULE_NAME);
		when(request.body()).thenReturn((new Gson()).toJson(new ScheduleRequest(TEST_CRON_EXPRESSION, jsonObject)));
		
		final Event[] event = new Event[1];
		
		doAnswer((InvocationOnMock invocation) -> event[0] = invocation.getArgument(0)).when(this.testLambda).schedule(any());
		assertEquals(Object.class, ScheduleController.createSchedule(request, response).getClass());
		
		assertEquals(TEST_SCHEDULE_NAME, event[0].getName());
		assertEquals(TEST_CRON_EXPRESSION, event[0].getCronExpression());
		assertEquals(this.testKey, event[0].getKey());
		assertEquals((new Gson()).toJson(jsonObject), event[0].getParameters());
		verify(response).status(201);
	}
	
	@Test
	public void updateSchedule() throws Exception {
		final Request  request  = mock(Request.class);
		final Response response = mock(Response.class);
		
		when(request.params(":user")).thenReturn(TEST_USER_NAME);
		when(request.params(":name")).thenReturn(TEST_LAMBDA_NAME);
		when(request.params(":event-name")).thenReturn(TEST_SCHEDULE_NAME);
		when(request.body()).thenReturn(new Gson().toJson(new ScheduleRequest("0 0 * * *", null)));
		
		final Event event = mock(Event.class);
		when(this.testLambda.getScheduledEvent(TEST_SCHEDULE_NAME)).thenReturn(Optional.of(event));
		
		assertEquals(Object.class, ScheduleController.updateSchedule(request, response).getClass());
		
		verify(event).setCronExpression("0 0 * * *");
		verifyNoMoreInteractions(event);
		verify(response).status(200);
	}
	
	@Test
	public void readSchedule() throws Exception {
		final Request    request    = mock(Request.class);
		final Response   response   = mock(Response.class);
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(TEST_PARAMETER_NAME, TEST_PARAMETER_VALUE);
		
		when(request.params(":user")).thenReturn(TEST_USER_NAME);
		when(request.params(":name")).thenReturn(TEST_LAMBDA_NAME);
		when(request.params(":event-name")).thenReturn(TEST_SCHEDULE_NAME);
		
		final Event event = mock(Event.class);
		when(event.getName()).thenReturn(TEST_SCHEDULE_NAME);
		when(event.getCronExpression()).thenReturn(TEST_CRON_EXPRESSION);
		when(event.getParameters()).thenReturn(new Gson().toJson(jsonObject));
		when(this.testLambda.getScheduledEvent(TEST_SCHEDULE_NAME)).thenReturn(Optional.of(event));
		
		final Object object     = ScheduleController.readSchedule(request, response);
		final Field  name       = object.getClass().getDeclaredField("name");
		final Field  parameters = object.getClass().getDeclaredField("parameters");
		final Field  calendar   = object.getClass().getDeclaredField("calendar");
		
		assertEquals(3, object.getClass().getDeclaredFields().length);
		name.setAccessible(true);
		parameters.setAccessible(true);
		calendar.setAccessible(true);
		
		assertEquals(TEST_SCHEDULE_NAME, name.get(object));
		assertEquals(TEST_CRON_EXPRESSION, calendar.get(object));
		assertEquals(new Gson().toJson(jsonObject), parameters.get(object));
		verify(response).status(200);
	}
	
	@Test
	public void deleteSchedule() throws Exception {
		
	}
	
	@Test
	public void getScheduledEvents() throws Exception {
		
	}
}