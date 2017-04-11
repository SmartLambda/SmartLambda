package edu.teco.smartlambda.integrationTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.schedule.Event;
import org.apache.commons.compress.utils.IOUtils;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * Created on 04.04.17.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {
	private static Thread smartLambdaApplication;
	private static final String smartLambdaURL     = "http://localhost:8080/";
	private static final String testUserName       = "IntegrationTestUser";
	private static       String testUserPrimaryKey = "";
	private static String testUserDeveloperKey;
	private static final String testUserDeveloperKeyName = "IntegrationTestDeveloper";
	private static final String testLambdaName           = "IntegrationTestLambda";
	private static final String testScheduleName         = "IntegrationTestSchedule";
	
	@BeforeClass
	public static void setupUser() throws Exception {
		deleteUserFromDatabase(testUserName);
		
		smartLambdaApplication = new Thread(Application::main);
		smartLambdaApplication.start();
		
		testUserPrimaryKey = registerTestUser(testUserName).get("primaryKey").getAsString();
	}
	
	@AfterClass
	public static void tearDown() {
		smartLambdaApplication.interrupt();
	}
	
	/*
	 * Delete User with the same name as testUserName, if present. Delete MonitoringEvents of this Users Keys before
	 */
	private static void deleteUserFromDatabase(final String username) {
		final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		if (!User.getByName(username).isPresent()) {
			if (session.getTransaction().isActive()) session.getTransaction().rollback();
			return;
		}
		
		final User user = User.getByName(username).get();
		
		final Key queryKey = from(Key.class);
		where(queryKey.getUser()).eq(user);
		final List<Key> keys = select(queryKey).list(session);
		
		for (final Key key : keys) {
			final MonitoringEvent queryEvent = from(MonitoringEvent.class);
			where(queryEvent.getKey()).eq(key);
			final List<MonitoringEvent> monitoringEvents = select(queryEvent).list(session);
			for (final MonitoringEvent event : monitoringEvents) {
				session.delete(event);
			}
		}
		
		session.delete(user);
		
		if (session.getTransaction().isActive()) session.getTransaction().commit();
	}
	
	private static JsonObject registerTestUser(final String name) throws UnirestException {
		final HashMap<String, Object> body       = new HashMap<>();
		final HashMap<String, String> parameters = new HashMap<>();
		parameters.put("name", name);
		body.put("parameters", parameters);
		body.put("identityProvider", "null");
		
		return requestJsonObject(RequestMethod.POST, "register", "null", "", body, 201, "Created");
	}
	
	public enum RequestMethod {
		GET, POST, PUT, PATCH, DELETE;
	}
	
	/*
	 * Creates a Unirest call. expectedStatus or expectedStatusText can be null to not assert any of them.
	 */
	private static JsonObject requestJsonObject(final RequestMethod method, final String path, final String headerName,
			final String headerValue, final Map<String, Object> body, final Integer expectedStatus, final String expectedStatusText)
			throws UnirestException {
		final HttpResponse<String> response = request(method, path, headerName, headerValue, body);
		if (expectedStatus != null) Assert.assertTrue(expectedStatus == response.getStatus());
		if (expectedStatusText != null) Assert.assertEquals(expectedStatusText, response.getStatusText());
		return new Gson().fromJson(response.getBody(), JsonObject.class);
	}
	
	/*
	 * Creates a Unirest call. expectedStatus or expectedStatusText can be null to not assert any of them.
	 */
	private static JsonPrimitive requestJsonPrimitive(final RequestMethod method, final String path, final String headerName,
			final String headerValue, final Map<String, Object> body, final Integer expectedStatus, final String expectedStatusText)
			throws UnirestException {
		final HttpResponse<String> response = request(method, path, headerName, headerValue, body);
		if (expectedStatus != null) Assert.assertTrue(expectedStatus == response.getStatus());
		if (expectedStatusText != null) Assert.assertEquals(expectedStatusText, response.getStatusText());
		return new Gson().fromJson(response.getBody(), JsonPrimitive.class);
	}
	
	/**
	 * Sends a Unirest call to smartLambdaURL with the supplied arguments and header "SmartLambda-Key", testUserPrimaryKey
	 */
	private static HttpResponse<String> request(final RequestMethod method, final String path, final String headerName,
			final String headerValue, final Map<String, Object> body) throws UnirestException {
		final HttpResponse<String> response;
		switch (method) {
			case POST:
				response = Unirest.post(smartLambdaURL + path).header(headerName, headerValue).body(new Gson().toJson(body)).asString();
				break;
			case GET:
				Assert.assertNull(body);
				response = Unirest.get(smartLambdaURL + path).header(headerName, headerValue).asString();
				break;
			case PUT:
				response = Unirest.put(smartLambdaURL + path).header(headerName, headerValue).body(new Gson().toJson(body)).asString();
				break;
			case PATCH:
				response = Unirest.patch(smartLambdaURL + path).header(headerName, headerValue).body(new Gson().toJson(body)).asString();
				break;
			case DELETE:
				response = Unirest.delete(smartLambdaURL + path).header(headerName, headerValue).body(new Gson().toJson(body)).asString();
				break;
			default:
				response = null;
				Assert.fail();
		}
		return response;
	}
	
	@Test
	public void _011_registerUserViaGitHubWithInvalidToken() throws Exception { //TFU010
		final HashMap<String, Object> body       = new HashMap<>();
		final HashMap<String, String> parameters = new HashMap<>();
		parameters.put("accessToken", "INVALID_ACCESS_TOKEN");
		body.put("parameters", parameters);
		body.put("identityProvider", "github");
		
		final JsonObject answer = requestJsonObject(RequestMethod.POST, "register", "null", "", body, 400, "Bad Request");
		Assert.assertTrue(answer.has("message"));
		Assert.assertEquals(answer.get("message").getAsString(), "HTTP/1.1 401 Unauthorized: {\"message\":\"Bad credentials\"," +
				"\"documentation_url\":\"https://developer.github.com/v3\"}");
	}
	
	@Test
	public void _01_registerUserViaNullIdentityProvider() throws Exception { //TF010
		final String userName = "IntegrationTest.registerUserViaNullIdentityProvider";
		
		deleteUserFromDatabase(userName);
		
		final JsonObject answer = registerTestUser(userName);
		Assert.assertEquals(answer.get("name").getAsString(), userName);
		Assert.assertNotNull(answer.get("primaryKey").getAsString());
	}
	
	@Test
	public void _02_createDeveloperKey() throws Exception { //TF020
		final String key =
				requestJsonPrimitive(RequestMethod.PUT, "key/" + testUserDeveloperKeyName, "SmartLambda-Key", testUserPrimaryKey, null,
						201,
						"Created").getAsString();
		Assert.assertNotNull(key);
		testUserDeveloperKey = key;
	}
	
	@Test
	public void _02b_createDeveloperKeyUnauthorized() throws Exception { //TFU020
		requestJsonObject(RequestMethod.PUT, "key/" + testUserDeveloperKeyName, "SmartLambda-Key", testUserDeveloperKey, null, 403,
				"Forbidden");
	}
	
	@Test
	public void _041_deployLambdaUnauthorized() throws Exception { //TFU024
		final HashMap<String, Object> body = new HashMap<>();
		body.put("async", "false");
		body.put("runtime", "jre8");
		body.put("src", IOUtils.toByteArray(IntegrationTest.class.getClassLoader().getResourceAsStream("lambda.jar")));
		
		final JsonObject answer =
				requestJsonObject(RequestMethod.PUT, testUserName + "/lambda/" + testLambdaName, "SmartLambda-Key", testUserDeveloperKey,
						body, 403, "Forbidden");
	}
	
	@Test
	public void _04_deployLambda() throws Exception { //TF024
		final HashMap<String, Object> body = new HashMap<>();
		body.put("async", "false");
		body.put("runtime", "jre8");
		body.put("src", IOUtils.toByteArray(IntegrationTest.class.getClassLoader().getResourceAsStream("lambda.jar")));
		
		final JsonObject answer =
				requestJsonObject(RequestMethod.PUT, testUserName + "/lambda/" + testLambdaName, "SmartLambda-Key", testUserPrimaryKey,
						body, 201, "Created");
		Assert.assertTrue(answer.entrySet().size() == 0);
	}
	
	@Test
	public void _04b_deployLambdaTwice() throws Exception {
		final HashMap<String, Object> body = new HashMap<>();
		body.put("async", "false");
		body.put("runtime", "jre8");
		body.put("src", IOUtils.toByteArray(IntegrationTest.class.getClassLoader().getResourceAsStream("lambda.jar")));
		
		final JsonObject answer =
				requestJsonObject(RequestMethod.PUT, testUserName + "/lambda/" + testLambdaName, "SmartLambda-Key", testUserPrimaryKey,
						body, 409, "Conflict");
	}
	
	@Test
	public void _191_deleteLambdaUnauthorized() throws Exception { //TFU060
		final JsonObject answer =
				requestJsonObject(RequestMethod.DELETE, testUserName + "/lambda/" + testLambdaName, "SmartLambda-Key",
						testUserDeveloperKey,
						null, 403, "Forbidden");
	}
	
	@Test
	public void _19_deleteLambda() throws Exception { //TF060
		final JsonObject answer =
				requestJsonObject(RequestMethod.DELETE, testUserName + "/lambda/" + testLambdaName, "SmartLambda-Key", testUserPrimaryKey,
						null, 200, "OK");
		Assert.assertTrue(answer.entrySet().size() == 0);
	}
	
	@Test
	public void _08_multiExecuteLambdaWithSameKey() throws Exception { //TF051
		final int             numberOfExecutors = 10;
		final ExecutorService threadPool        = Executors.newFixedThreadPool(numberOfExecutors);
		final LinkedList<Future> futureList = new LinkedList<>();
		
		for (int i = 0; i < numberOfExecutors; i++) {
			futureList.add(threadPool.submit(() -> {
				final HashMap<String, Object> body = new HashMap<>();
				body.put("async", "false");
				body.put("parameters", Collections.singletonMap("demoValue", "value"));
				final JsonObject response;
				try {
					response = requestJsonObject(RequestMethod.POST, testUserName + "/lambda/" + testLambdaName, "SmartLambda-Key",
							testUserPrimaryKey, body, 200, "OK");
					Assert.assertNotNull(response);
					Assert.assertEquals(response.get("demoReturnValue").getAsString(), "success");
				} catch (UnirestException e) {
					Assert.fail();
				}
			}));
		}
		
		while (!futureList.isEmpty()) {
			if (futureList.element().isDone()) futureList.remove();
		}
	}
	
	@Test
	public void _121_unauthorizedSchedule() throws Exception { //TFU091
		final JsonObject answer =
				requestJsonObject(RequestMethod.PUT, testUserName + "/lambda/" + testLambdaName + "/schedule/" + testScheduleName,
						"SmartLambda-Key", testUserDeveloperKey,
						Collections.singletonMap("parameters", Collections.singletonMap("demoValue", "")), 403, "Forbidden");
	}
	
	@Test
	public void _12_schedule() throws Exception { //TF091
		final HashMap<String, Object> body           = new HashMap<>();
		final int                     schedulingHour = (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 1) % 24;
		body.put("calendar", "0 0/5 " + schedulingHour + " * * ?");
		body.put("parameters", Collections.singletonMap("demoValue", ""));
		
		final JsonObject answer =
				requestJsonObject(RequestMethod.PUT, testUserName + "/lambda/" + testLambdaName + "/schedule/" + testScheduleName,
						"SmartLambda-Key", testUserPrimaryKey, body, 201, "Created");
		Assert.assertTrue(answer.entrySet().size() == 0);
		Assert.assertEquals(this.getNextScheduledExecution(testScheduleName).get(Calendar.HOUR_OF_DAY), schedulingHour);
	}
	
	@Test
	public void _13_updateSchedule() throws Exception { //TF092
		final HashMap<String, Object> body           = new HashMap<>();
		final int                     schedulingHour = (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 1) % 24;
		body.put("calendar", "0 0/5 " + schedulingHour + " * * ?");
		
		final JsonObject answer =
				requestJsonObject(RequestMethod.PATCH, testUserName + "/lambda/" + testLambdaName + "/schedule/" + testScheduleName,
						"SmartLambda-Key", testUserPrimaryKey, body, 200, "OK");
		Assert.assertTrue(answer.entrySet().size() == 0);
		Assert.assertEquals(this.getNextScheduledExecution(testScheduleName).get(Calendar.HOUR_OF_DAY), schedulingHour);
	}
	
	@Test
	public void _141_unauthorizedScheduleDeletion() throws Exception { //TFU093
		final JsonObject answer =
				requestJsonObject(RequestMethod.DELETE, testUserName + "/lambda/" + testLambdaName + "/schedule/" + testScheduleName,
						"SmartLambda-Key", testUserDeveloperKey, null, 403, "Forbidden");
		Assert.assertTrue(answer.entrySet().size() == 0);
	}
	
	@Test
	public void _14_deleteSchedule() throws Exception { //TF093
		final JsonObject answer =
				requestJsonObject(RequestMethod.DELETE, testUserName + "/lambda/" + testLambdaName + "/schedule/" + testScheduleName,
						"SmartLambda-Key", testUserPrimaryKey, null, 200, "OK");
		Assert.assertTrue(answer.entrySet().size() == 0);
	}
	
	private Calendar getNextScheduledExecution(final String name) {
		final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		final Event query = from(Event.class);
		where(query.getName()).eq(name);
		final Optional<Event> event = select(query).get(session);
		if (!event.isPresent()) {
			if (session.getTransaction().isActive()) session.getTransaction().rollback();
			Assert.fail();
		}
		final Calendar result = event.get().getNextExecution();
		if (session.getTransaction().isActive()) session.getTransaction().rollback();
		return result;
	}
	
	@Test
	public void _23_addDeveloperPermissions() throws Exception { //TF023
		final HashMap<String, Object> body             = new HashMap<>();
		final HashMap<String, Object> firstParameter = new HashMap<>();
		final HashMap[]               parameters       = {firstParameter};
		firstParameter.put("user", testUserName);
		firstParameter.put("name", "*");
		body.put("execute", parameters);
		
		final JsonObject answer =
				requestJsonObject(RequestMethod.PUT, "key/" + testUserDeveloperKeyName + "/permissions", "SmartLambda-Key",
						testUserPrimaryKey, body, 200, "OK");
		Assert.assertTrue(answer.entrySet().size() == 0);
	}
}