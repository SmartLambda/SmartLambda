package edu.teco.smartlambda.rest.controller;

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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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
		private final String name;
		private final String calendar;
		private final String parameters;
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
		
		assertNotNull(event[0]);
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
		
		verify(this.testLambda).schedule(event);
		verify(event).setCronExpression("0 0 * * *");
		verifyNoMoreInteractions(event);
		verify(response).status(200);
	}
	
	private ScheduleResponse validateScheduleResponseObject(final Object object) throws Exception {
		final Field name       = object.getClass().getDeclaredField("name");
		final Field parameters = object.getClass().getDeclaredField("parameters");
		final Field calendar   = object.getClass().getDeclaredField("calendar");
		
		assertEquals(3, object.getClass().getDeclaredFields().length);
		
		assertNotNull(name);
		assertSame(String.class, name.getType());
		assertNotNull(parameters);
		assertSame(String.class, parameters.getType());
		assertNotNull(calendar);
		assertSame(String.class, calendar.getType());
		
		name.setAccessible(true);
		parameters.setAccessible(true);
		calendar.setAccessible(true);
		
		return new ScheduleResponse((String) name.get(object), (String) calendar.get(object), (String) parameters.get(object));
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
		
		final ScheduleResponse scheduleResponse = this.validateScheduleResponseObject(ScheduleController.readSchedule(request, response));
		
		assertEquals(TEST_SCHEDULE_NAME, scheduleResponse.getName());
		assertEquals(TEST_CRON_EXPRESSION, scheduleResponse.getCalendar());
		assertEquals(new Gson().toJson(jsonObject), scheduleResponse.getParameters());
		verify(response).status(200);
	}
	
	@Test
	public void deleteSchedule() throws Exception {
		final Request  request  = mock(Request.class);
		final Response response = mock(Response.class);
		
		when(request.params(":user")).thenReturn(TEST_USER_NAME);
		when(request.params(":name")).thenReturn(TEST_LAMBDA_NAME);
		when(request.params(":event-name")).thenReturn(TEST_SCHEDULE_NAME);
		
		final Event event = mock(Event.class);
		when(this.testLambda.getScheduledEvent(TEST_SCHEDULE_NAME)).thenReturn(Optional.of(event));
		
		assertEquals(Object.class, ScheduleController.deleteSchedule(request, response).getClass());
		
		verify(event).delete();
		verifyNoMoreInteractions(event);
		verify(response).status(200);
	}
	
	@Test
	public void getScheduledEvents() throws Exception {
		final Request    request    = mock(Request.class);
		final Response   response   = mock(Response.class);
		final JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(TEST_PARAMETER_NAME, TEST_PARAMETER_VALUE);
		
		when(request.params(":user")).thenReturn(TEST_USER_NAME);
		when(request.params(":name")).thenReturn(TEST_LAMBDA_NAME);
		
		final List<Event> events = new LinkedList<>();
		Event             event  = mock(Event.class);
		
		when(event.getName()).thenReturn(TEST_SCHEDULE_NAME);
		when(event.getParameters()).thenReturn(null);
		when(event.getCronExpression()).thenReturn(TEST_CRON_EXPRESSION);
		
		events.add(event);
		
		event = mock(Event.class);
		when(event.getName()).thenReturn(TEST_SCHEDULE_NAME + "2");
		when(event.getParameters()).thenReturn(new Gson().toJson(jsonObject));
		when(event.getCronExpression()).thenReturn("0 * * * *");
		
		events.add(event);
		
		when(this.testLambda.getScheduledEvents()).thenReturn(events);
		
		final Object result = ScheduleController.getScheduledEvents(request, response);
		assertTrue(result instanceof Collection);
		final Collection collection = (Collection) result;
		assertEquals(2, collection.size());
		final Iterator iterator = collection.iterator();
		
		final String nextName;
		final String nextParameters;
		final String nextCronExpression;
		
		ScheduleResponse scheduleResponse = this.validateScheduleResponseObject(iterator.next());
		
		if (scheduleResponse.getName().equals(TEST_SCHEDULE_NAME + "2")) {
			nextName = TEST_SCHEDULE_NAME;
			nextParameters = null;
			nextCronExpression = TEST_CRON_EXPRESSION;
			
			assertEquals(TEST_SCHEDULE_NAME + "2", scheduleResponse.getName());
			assertEquals(new Gson().toJson(jsonObject), scheduleResponse.getParameters());
			assertEquals("0 * * * *", scheduleResponse.getCalendar());
		} else {
			nextName = TEST_SCHEDULE_NAME + "2";
			nextParameters = new Gson().toJson(jsonObject);
			nextCronExpression = "0 * * * *";
			
			assertEquals(TEST_SCHEDULE_NAME, scheduleResponse.getName());
			assertNull(scheduleResponse.getParameters());
			assertEquals(TEST_CRON_EXPRESSION, scheduleResponse.getCalendar());
		}
		
		scheduleResponse = this.validateScheduleResponseObject(iterator.next());
		
		assertEquals(nextName, scheduleResponse.getName());
		assertEquals(nextParameters, scheduleResponse.getParameters());
		assertEquals(nextCronExpression, scheduleResponse.getCalendar());
	}
}