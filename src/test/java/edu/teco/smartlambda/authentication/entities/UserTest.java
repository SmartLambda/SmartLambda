package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.authentication.AuthenticationService;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by Matteo on 07.02.2017.
 */
public class UserTest {
	
	AuthenticationService service = AuthenticationService.getInstance();
	User user = new User("UserTest.User");
	
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