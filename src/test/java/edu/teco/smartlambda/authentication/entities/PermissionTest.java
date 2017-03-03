package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;

/**
 * Created by Matteo on 07.02.2017.
 */
public class PermissionTest {
	@Before
	public void buildUp() {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		//
	}
	
	@After
	public void tearDown() throws Exception {
		Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
}