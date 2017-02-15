package edu.teco.smartlambda.authentication.entities;

import org.junit.Test;

/**
 * Created by Matteo on 07.02.2017.
 */
public class UserTest {
	
	User user = new User();
	
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