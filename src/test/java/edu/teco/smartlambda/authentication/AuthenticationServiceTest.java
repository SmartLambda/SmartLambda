package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
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
		executorService = Executors.newSingleThreadExecutor();
	}
	
	@After
	public void tearDown() throws Exception {
		Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void getInstanceNotNull() throws Exception {
		Assert.assertNotNull(AuthenticationService.getInstance());
	}
	
	@Test
	public void getInstanceReturnsSingleton() throws Exception {
		AuthenticationService asFirst = AuthenticationService.getInstance();
		Assert.assertNotNull(asFirst);
		AuthenticationService asSecond = AuthenticationService.getInstance();
		Assert.assertNotNull(asSecond);
		Assert.assertSame(asFirst, asSecond);
	}
	
	@Test
	public void getInstanceSingletonIsThreadlocal() throws Exception {
		AuthenticationService asFirst = AuthenticationService.getInstance();
		Assert.assertNotNull(asFirst);
		
		final Future<AuthenticationService> future = executorService.submit(AuthenticationService::getInstance);
		
		Assert.assertNotNull(future.get());
		Assert.assertNotSame(asFirst, future.get());
	}
	
	@Test
	public void authenticateViaParimaryKey() throws Exception {
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		Map<String, String>         params                = new HashMap<>();
		params.put("name", "AuthenticationServiceTest.authenticateViaPrimaryKey.User");
		final User user = IdentityProviderRegistry.getInstance().getIdentityProviderByName(NullIdentityProvider.class.getName()).register
				(params).getLeft();
		authenticationService.authenticate(user.getPrimaryKey());
		assert authenticationService.getAuthenticatedKey().isPresent();
		Assert.assertSame(user.getPrimaryKey(), authenticationService.getAuthenticatedKey().get());
	}
	
	@Test
	public void authenticateViaKey() throws Exception {
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		Map<String, String>         params                = new HashMap<>();
		params.put("name", "AuthenticationServiceTest.authenticateViaKey.User");
		final User user = IdentityProviderRegistry.getInstance().getIdentityProviderByName(NullIdentityProvider.class.getName()).register
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
		Map<String, String>         params                = new HashMap<>();
		params.put("name", "AuthenticationServiceTest.authenticateViaString.User");
		final Pair<User, String> pair = IdentityProviderRegistry.getInstance().getIdentityProviderByName(NullIdentityProvider.class.getName
				()).register(params);
		final User user = pair.getLeft();
		final AuthenticationService authenticationService = AuthenticationService.getInstance();
		Assert.assertFalse(authenticationService.getAuthenticatedKey().isPresent());
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