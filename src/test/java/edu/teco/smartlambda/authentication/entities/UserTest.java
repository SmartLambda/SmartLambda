package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.authentication.AuthenticationService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Matteo on 07.02.2017.
 */
public class UserTest {
	
	static AuthenticationService service;
	static Map                   params;
	static User                  user;
	
	@BeforeClass
	public static void initialize() {
		service = AuthenticationService.getInstance();
		params     = new HashMap<String, String>();
		params.put("name", "UserTest.User");
		user    = new User(params);
	}
	@Before
	public void buildUp() {
		
		service.authenticate(user.getPrimaryKey());
	}
		
	
	@Test
	public void createKey() throws Exception {
		
		Key key = user.createKey("UserTest.createKey").getLeft();
		/*
			TODO: insert key in the database and check if it's there
		 */
	}
	
	@Test
	public void getVisibleUsers() throws Exception {
		
		
		
	}
}