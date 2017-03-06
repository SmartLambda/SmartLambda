package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.identity.NullIdentityProvider;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class UserTest {
	
	private static AuthenticationService service;
	private static Map                   params;
	private static User                  user;

	@Before
	public void buildUp() {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		service = AuthenticationService.getInstance();
		params     = new HashMap<String, String>();
		params.put("name", "UserTest.User");
		user = (User) new NullIdentityProvider().register(params)
				.getLeft();//TODO WTF!?
		service.authenticate(user.getPrimaryKey());
	}
	
	@After
	public void tearDown() throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void createKey() throws Exception {
		
		final Key key = user.createKey("UserTest.createKey").getLeft();
		/*
			TODO: insert key in the database and check if it's there
		 */
	}
	
	@Test
	public void getVisibleUsers() throws Exception {
		
		
		
	}
}