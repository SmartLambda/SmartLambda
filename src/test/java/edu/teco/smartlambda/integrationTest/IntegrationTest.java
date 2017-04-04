package edu.teco.smartlambda.integrationTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;

/**
 * Created on 04.04.17.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {
	private final        String smartLambdaURL = "http://localhost:8080";
	private static final String testUserName   = "IntegrationTestUser";
	private String testUserPrimaryKey;
	
	@BeforeClass
	public static void setupTestUser() throws Exception {
		//Delete User with the same name as testUserName, if present.
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		User.getByName(testUserName).ifPresent(user -> Application.getInstance().getSessionFactory().getCurrentSession().delete(user));
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.commit();
	}
	
	@Test
	public void tf010NullIdentityProvider() throws Exception {
		final HttpResponse response = this.registerTestUser();
		Assert.assertEquals(response.getStatus(), 201);
		Assert.assertEquals(response.getStatusText(), "Created");
		final Gson       gson       = new Gson();
		final JsonObject jsonObject = gson.fromJson(response.getBody().toString(), JsonObject.class);
		Assert.assertEquals(jsonObject.get("name").getAsString(), testUserName);
		this.testUserPrimaryKey = jsonObject.get("primaryKey").getAsString();
		System.out.println(this.testUserPrimaryKey);
	}
	
	private HttpResponse registerTestUser() throws UnirestException {
		final HashMap<String, Object> body       = new HashMap<>();
		final HashMap<String, String> parameters = new HashMap<>();
		parameters.put("name", testUserName);
		body.put("parameters", parameters);
		body.put("identityProvider", "null");
		
		final Gson   gson       = new Gson();
		final String bodyString = gson.toJson(body);
		
		return Unirest.post(this.smartLambdaURL + "/register").header("Content-Type", "application/json").body(bodyString).asString();
	}
}
