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
import org.apache.commons.compress.utils.IOUtils;
import org.hibernate.Session;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public void _1_registerUserViaNullIdentityProvider() throws Exception { //TF010
		final String userName = "IntegrationTest.registerUserViaNullIdentityProvider";
		
		deleteUserFromDatabase(userName);
		
		final JsonObject answer = registerTestUser(userName);
		Assert.assertEquals(answer.get("name").getAsString(), userName);
		Assert.assertNotNull(answer.get("primaryKey").getAsString());
	}
	
	@Test
	public void _2_createDeveloperKey() throws Exception { //TF020
		final String key =
				requestJsonPrimitive(RequestMethod.PUT, "key/" + testUserDeveloperKeyName, "SmartLambda-Key", testUserPrimaryKey, null,
						201,
						"Created").getAsString();
		Assert.assertNotNull(key);
		testUserDeveloperKey = key;
	}
	
	@Test
	public void _4_deployLambda() throws Exception { //TF024
		final HashMap<String, Object> body = new HashMap<>();
		body.put("async", "false");
		body.put("runtime", "jre8");
		body.put("src", IOUtils.toByteArray(IntegrationTest.class.getClassLoader().getResourceAsStream("lambda.jar")));
		
		final JsonObject answer =
				requestJsonObject(RequestMethod.PUT, testUserName + "/lambda/" + testLambdaName, "SmartLambda-Key", testUserPrimaryKey,
						body, 201, "Created");
		Assert.assertTrue(answer.entrySet().size() == 0);
	}
}