package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.utility.TestUtility;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 28.02.17.
 */
public class NullIdentityProviderTest {
	
	@Before
	public void setUp() throws Exception {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
	}
	
	@After
	public void tearDown() throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void testReturnValue() {
		final IdentityProvider    ip     = new NullIdentityProvider();
		final Map<String, String> params = new HashMap<>();
		final String              input  = "testname123";
		params.put("name", input);
		final User user = ip.register(params).getLeft();
		Assert.assertEquals(input, user.getName());
	}
	
	@Test (expected = IdentitySyntaxException.class)
	public void testNullParameter() {
		new NullIdentityProvider().register(null);
	}
	
	@Test (expected = IdentitySyntaxException.class)
	public void testEmptyParameter() {
		new NullIdentityProvider().register(new HashMap<>());
	}
	
	@Test
	public void testGettersAndSetters() throws Exception{
		TestUtility.testGettersAndSetters(new NullIdentityProvider());
	}
}