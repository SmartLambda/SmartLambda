package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.utility.TestUtility;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class PermissionTest {
	@Before
	public void buildUp() {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
	}
	
	@After
	public void tearDown() throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void testGettersAndSetters() throws Exception{
		final User       user       = User.createUser("").getLeft();
		final Permission permission = new Permission(user, PermissionType.CREATE, user.getPrimaryKey());
		TestUtility.testGettersAndSetters(permission);
	}
	
}