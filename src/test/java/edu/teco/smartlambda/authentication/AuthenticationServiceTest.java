package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Created by Jonathan on 10.02.17.
 */
public class AuthenticationServiceTest {
	AuthenticationService authenticationService = null;
	User user = null;
	
	@Before
	public void setUp() throws Exception {
		authenticationService = null;
		user = null;
	}
	
	@After
	public void tearDown() throws Exception {
		authenticationService = null;
		user = null;
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
				user = new User();//TODO also use an existing User an Key
				try {
					Key key = user.createKey("AuthenticationServiceTest.authenticateViaKey").getLeft();
					authenticationService.authenticate(key);
					//checking for the Result after setting Key twice
					authenticationService.authenticate(key);
				} catch (InsufficientPermissionsException i) {
					Assert.fail("InsufficientPermissionsException");
				} catch (NameConflictException n) {
					Assert.fail("NameConflictException");
				}

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
				user = new User();//TODO also use an existing User an Key
				try {
					Key key = user.createKey("AuthenticationServiceTest.getAuthenticatedKeyViaKey").getLeft();
					authenticationService.authenticate(key);
					Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
					Assert.assertTrue(keyOpt.isPresent());
					Assert.assertEquals(keyOpt.get(), key);
				} catch (InsufficientPermissionsException i) {
					Assert.fail("InsufficientPermissionsException");
				} catch (NameConflictException n) {
					Assert.fail("NameConflictException");
				}
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
				Key authenticatedKey = keyOpt.get();
				
				try {
					Method m = authenticatedKey.getClass().getDeclaredMethod("getId");
					
					m.setAccessible(true);
					String authenticatedKeyID = (String) m.invoke(authenticatedKey);
					m.setAccessible(false);
					
					Assert.assertEquals(authenticatedKeyID, ""/*TODO use an existing Key*/ );
				} catch (NoSuchMethodException n) {
					Assert.fail("NoSuchMethodException");
				} catch (IllegalAccessException i ) {
					Assert.fail("IllegalAccessException");
				} catch (InvocationTargetException i) {
					Assert.fail("InvocationTargetException");
				}
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
				user = new User();//TODO also use an existing User an Key
				try {
					Key key = user.createKey("AuthenticationServiceTest.getAuthenticatedUserViaKey").getLeft();
					authenticationService.authenticate(key);
					Optional<Key> keyOpt = authenticationService.getAuthenticatedKey();
					Assert.assertTrue(keyOpt.isPresent());
					Assert.assertEquals(keyOpt.get().getUser(), key.getUser());
				} catch (InsufficientPermissionsException i) {
					Assert.fail("InsufficientPermissionsException");
				} catch (NameConflictException n) {
					Assert.fail("NameConflictException");
				}
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