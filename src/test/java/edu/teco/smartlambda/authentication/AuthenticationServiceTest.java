package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.identity.NullIdentityProvider;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created on 10.02.17.
 */
public class AuthenticationServiceTest {
	
	private ExecutorService executorService;
	
	@Before
	public void setUp() throws Exception {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		this.executorService = Executors.newSingleThreadExecutor();
	}
	
	@After
	public void tearDown() throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void getInstanceNotNull() throws Exception {
		Assert.assertNotNull(AuthenticationService.getInstance());
	}
	
	@Test
	public void getInstanceReturnsSingleton() throws Exception {
		final AuthenticationService asFirst = AuthenticationService.getInstance();
		Assert.assertNotNull(asFirst);
		final AuthenticationService asSecond = AuthenticationService.getInstance();
		Assert.assertNotNull(asSecond);
		Assert.assertSame(asFirst, asSecond);
	}
	
	@Test
	public void getInstanceSingletonIsThreadlocal() throws Exception {
		final AuthenticationService asFirst = AuthenticationService.getInstance();
		Assert.assertNotNull(asFirst);
		
		final Future<AuthenticationService> future = this.executorService.submit(AuthenticationService::getInstance);
		
		Assert.assertNotNull(future.get());
		Assert.assertNotSame(asFirst, future.get());
	}
	
	@Test
	public void AuthenticationServiceHasNoKeyAtStart() throws Exception {
		final Future<AuthenticationService> future = this.executorService.submit(AuthenticationService::getInstance);
		
		Assert.assertFalse(future.get().getAuthenticatedKey().isPresent());
	}
	
	@Test
	public void authenticateViaParimaryKey() throws Exception {
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		final Map<String, String>   params                = new HashMap<>();
		params.put("name", "AuthenticationServiceTest.authenticateViaPrimaryKey.User");
		final User user = new NullIdentityProvider().register
				(params).getLeft();
		authenticationService.authenticate(user.getPrimaryKey());
		assert authenticationService.getAuthenticatedKey().isPresent();
		Assert.assertSame(user.getPrimaryKey(), authenticationService.getAuthenticatedKey().get());
	}
	
	@Test
	public void authenticateViaKey() throws Exception {
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		final Map<String, String>   params                = new HashMap<>();
		params.put("name", "AuthenticationServiceTest.authenticateViaKey.User");
		final User user = new NullIdentityProvider().register
				(params).getLeft();
		authenticationService.authenticate(user.getPrimaryKey());
		assert authenticationService.getAuthenticatedKey().isPresent();
		Assert.assertSame(user.getPrimaryKey(), authenticationService.getAuthenticatedKey().get());
		final Key key = user.createKey("AuthenticationServiceTest.authenticateViaKey").getLeft();
		authenticationService.authenticate(key);
		assert authenticationService.getAuthenticatedKey().isPresent();
		Assert.assertSame(key, authenticationService.getAuthenticatedKey().get());
		//Checking double authentication
		authenticationService.authenticate(key);
		assert authenticationService.getAuthenticatedKey().isPresent();
		Assert.assertSame(key, authenticationService.getAuthenticatedKey().get());
	}
	
	@Test
	public void authenticateViaString() throws Exception {
		final Map<String, String> params = new HashMap<>();
		params.put("name", "AuthenticationServiceTest.authenticateViaString.User");
		final Pair<User, String>    pair                  = new NullIdentityProvider().register(params);
		final User                  user                  = pair.getLeft();
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		authenticationService.authenticate(pair.getRight());
		assert authenticationService.getAuthenticatedKey().isPresent();
		Assert.assertSame(user.getPrimaryKey(), authenticationService.getAuthenticatedKey().get());
		//Checking double authentication
		authenticationService.authenticate(pair.getRight());
		assert authenticationService.getAuthenticatedKey().isPresent();
		Assert.assertSame(user.getPrimaryKey(), authenticationService.getAuthenticatedKey().get());
	}

	
	@Test(expected=NotAuthenticatedException.class)
	public void getAuthenticatedUserViaWrongString() throws Exception {
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		authenticationService.authenticate("This is a non existing Key");
	}
}