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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * Created on 04.04.17.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {
	private static final String smartLambdaURL = "http://localhost:8080/";
	private static final String testUserName   = "IntegrationTestUser";
	private static String testUserPrimaryKey;
	private static String testUserDeveloperKey;
	private static final String testUserDeveloperKeyName = "IntegrationTestDeveloper";
	private static final String testLambdaName = "IntegrationTestLambda";
	
	@BeforeClass
	public static void setupTestUser() throws Exception {
		deleteUserFromDatabase(testUserName);
		
		//Create new Account
		final HttpResponse response = registerTestUser(testUserName);
		assert response.getStatus() == 201;
		final JsonObject jsonObject = new Gson().fromJson(response.getBody().toString(), JsonObject.class);
		testUserPrimaryKey = jsonObject.get("primaryKey").getAsString();
	}
	
	/*
	 * Delete User with the same name as testUserName, if present. Delete MonitoringEvents of this Users Keys before
	 */
	private static void deleteUserFromDatabase (final String username) {
		final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		if (!User.getByName(username).isPresent()) {
			if (session.getTransaction().isActive()) session.getTransaction().rollback();
			return;
		}
		
		final User user = User.getByName(username).get();
		
		final Key     queryKey   = from(Key.class);
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
	
	private static HttpResponse registerTestUser(final String name) throws UnirestException {
		final HashMap<String, Object> body       = new HashMap<>();
		final HashMap<String, String> parameters = new HashMap<>();
		parameters.put("name", name);
		body.put("parameters", parameters);
		body.put("identityProvider", "null");
		
		final String bodyString = new Gson().toJson(body);
		
		return Unirest.post(smartLambdaURL + "register").body(bodyString).asString();
	}
	
	@Test
	public void _1_registerUserViaNullIdentityProvider() throws Exception { //TF010
		final String userName = "IntegrationTest.registerUserViaNullIdentityProvider";
		
		deleteUserFromDatabase(userName);
		
		final HttpResponse response = registerTestUser(userName);
		Assert.assertEquals(response.getStatus(), 201);
		Assert.assertEquals(response.getStatusText(), "Created");
		final JsonObject jsonObject = new Gson().fromJson(response.getBody().toString(), JsonObject.class);
		Assert.assertEquals(jsonObject.get("name").getAsString(), userName);
		Assert.assertNotNull(jsonObject.get("primaryKey").getAsString());
	}
	
	@Test
	public void _2_createDeveloperKey() throws Exception { //TF020
		final HttpResponse<String> response =
				Unirest.put(smartLambdaURL + "key/" + testUserDeveloperKeyName).header("SmartLambda-Key", testUserPrimaryKey).asString();
		Assert.assertEquals(201, response.getStatus());
		Assert.assertEquals("Created", response.getStatusText());
		
		final String key = new Gson().fromJson(response.getBody(), JsonPrimitive.class).getAsString();
		Assert.assertNotNull(key);
		testUserDeveloperKey = key;
	}
	
	@Test
	public void _4_deployLambda() throws Exception { //TF024
		final HashMap<String, Object> body       = new HashMap<>();
		body.put("async", "false");
		body.put("runtime", "jre8");
		body.put("src", IOUtils.toByteArray(IntegrationTest.class.getClassLoader().getResourceAsStream("lambda.jar")));
		final String bodyString = new Gson().toJson(body);
		
		final HttpResponse<String> response =
				Unirest.put(smartLambdaURL + testUserName + "/lambda/" + testLambdaName).header("SmartLambda-Key", testUserPrimaryKey)
						.body(bodyString).asString();
		System.out.println(response.getBody());
		Assert.assertEquals(201, response.getStatus());
		Assert.assertEquals("Created", response.getStatusText());
		
		final JsonObject jsonObject = new Gson().fromJson(response.getBody(), JsonObject.class);
		Assert.assertTrue(jsonObject.entrySet().size() == 0);
	}
}