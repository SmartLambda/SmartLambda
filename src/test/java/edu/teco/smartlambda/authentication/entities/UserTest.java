package edu.teco.smartlambda.authentication.entities;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Matteo on 07.02.2017.
 */
public class UserTest {
	
	User user;
	
	@Test
	public void createKey() throws Exception {
		
		Key key = new Key("abc", user);
		/*
			TODO: insert key in the database and check if it's there
		 */
	}
	
	@Test
	public void getVisibleUsers() throws Exception {
		
		
		
	}
}