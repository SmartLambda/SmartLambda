package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.authentication.entities.Key;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by Jonathan on 10.02.17.
 */
public class AuthenticationServiceTest {
	AuthenticationService authenticationService = null;
	
	@Before
	public void setUp() throws Exception {
		authenticationService = null;
	}
	
	@After
	public void tearDown() throws Exception {
		authenticationService = null;
	}
	
	@Test
	public void getInstanceNotNull() throws Exception {
		Assert.assertNotNull(AuthenticationService.getInstance());
	}
	
	@Test
	public void getInstanceReturnsSingelton() throws Exception {
		AuthenticationService asFirst = AuthenticationService.getInstance();
		Assert.assertNotNull(asFirst);
		AuthenticationService asSecond = AuthenticationService.getInstance();
		Assert.assertNotNull(asSecond);
		Assert.assertEquals(asFirst, asSecond);
	}
	
	@Test
	public void getInstanceSingeltonIsThreadlocal() throws Exception {
		AuthenticationService asFirst = AuthenticationService.getInstance();
		Assert.assertNotNull(asFirst);
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				authenticationService = AuthenticationService.getInstance();
			}
		});
		thread.start();
		thread.join();
		Assert.assertNotNull(authenticationService);
		Assert.assertNotEquals(asFirst, authenticationService);
	}
	
	@Test
	public void authenticateViaKey() throws Exception {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				authenticationService = AuthenticationService.getInstance();
				Key key = new Key("");//TODO also use an existing Key
				authenticationService.authenticate(key);
				//checking for the Result after setting Key twice
				authenticationService.authenticate(key);
			}
		});
		thread.start();
		thread.join();
	}
	
	@Test
	public void authenticateViaString() throws Exception {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				authenticationService = AuthenticationService.getInstance();
				authenticationService.authenticate(""/*TODO also use an existing Key*/);
				//checking for the Result after setting Key twice
				authenticationService.authenticate("");
			}
		});
		thread.start();
		thread.join();
	}
	
	@Test
	public void getAuthenticatedKeyViaKey() throws Exception {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				authenticationService = AuthenticationService.getInstance();
				Key key = new Key("");//TODO also use an existing Key
				authenticationService.authenticate(key);
				Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
				Assert.assertTrue(keyOpt.isPresent());
				Assert.assertEquals(keyOpt.get(), key);
			}
		});
		thread.start();
		thread.join();
	}
	
	@Test
	public void getAuthenticatedKeyViaString() throws Exception {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				authenticationService = AuthenticationService.getInstance();
				authenticationService.authenticate("");//TODO use an existing Key
				Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
				Assert.assertTrue(keyOpt.isPresent());
				Assert.assertEquals(keyOpt.get().getId(), ""/*TODO use an existing Key*/ );
			}
		});
		thread.start();
		thread.join();
	}
	
	@Test
	public void getAuthenticatedUserViaKey() throws Exception {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				authenticationService = AuthenticationService.getInstance();
				Key key = new Key("");//TODO also use an existing Key
				authenticationService.authenticate(key);
				Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
				Assert.assertTrue(keyOpt.isPresent());
				Assert.assertEquals(keyOpt.get().getUser(), key.getUser());
			}
		});
		thread.start();
		thread.join();
	}
	
	@Test
	public void getAuthenticatedUserViaString() throws Exception {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				authenticationService = AuthenticationService.getInstance();
				authenticationService.authenticate("");//TODO use an existing Key
				Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
				Assert.assertTrue(keyOpt.isPresent());
				Assert.assertEquals(keyOpt.get().getUser(), ""/*TODO use the User of the Key*/ );
			}
		});
		thread.start();
		thread.join();
	}
}