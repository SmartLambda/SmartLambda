package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.security.MessageDigest;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created on 10.02.17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MessageDigest.class, Key.class})
@PowerMockIgnore({"javax.xml.*", "org.xml.sax.*", "org.w3c.*", "javax.management.*"})
public class AuthenticationServiceTest {
	
	private AuthenticationService authenticationService;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@BeforeClass
	public static void beforeClass() {
		mockStatic(MessageDigest.class);
	}
	
	@Before
	public void setUp() throws Exception {
		// obtain a fresh instance of AuthenticationService before each test
		this.authenticationService = Executors.newSingleThreadExecutor().submit(AuthenticationService::getInstance).get();
	}
	
	@Test
	public void getInstanceNotNull() throws Exception {
		assertNotNull(this.authenticationService);
	}
	
	@Test
	public void getInstanceReturnsSingleton() throws Exception {
		final AuthenticationService asFirst = this.authenticationService;
		assertNotNull(asFirst);
		final AuthenticationService asSecond = this.authenticationService;
		assertNotNull(asSecond);
		assertSame(asFirst, asSecond);
	}
	
	@Test
	public void getInstanceSingletonIsThreadlocal() throws Exception {
		final AuthenticationService asFirst = AuthenticationService.getInstance();
		assertNotNull(asFirst);
		
		assertNotNull(this.authenticationService);
		assertNotSame(asFirst, this.authenticationService);
	}
	
	@Test
	public void AuthenticationServiceIsEmptyAtStart() throws Exception {
		assertFalse(this.authenticationService.getAuthenticatedKey().isPresent());
		assertFalse(this.authenticationService.getAuthenticatedUser().isPresent());
	}
	
	@Test
	public void authenticateViaParimaryKey() throws Exception {
		final Key primaryKey = mock(Key.class);
		when(primaryKey.isPrimaryKey()).thenReturn(true);
		this.authenticationService.authenticate(primaryKey);
		
		assertTrue(this.authenticationService.getAuthenticatedKey().isPresent());
		assertTrue(this.authenticationService.getAuthenticatedKey().get().isPrimaryKey());
		assertSame(primaryKey, this.authenticationService.getAuthenticatedKey().get());
	}
	
	@Test
	public void authenticateViaKey() throws Exception {
		final Key key = mock(Key.class);
		when(key.isPrimaryKey()).thenReturn(false);
		
		this.authenticationService.authenticate(key);
		assertTrue(this.authenticationService.getAuthenticatedKey().isPresent());
		assertSame(key, this.authenticationService.getAuthenticatedKey().get());
	}
	
	@Test
	public void authenticateViaString() throws Exception {
		final String someKeyString = "veryKeySuchAuthenticatedMuchWow";
		final Key    key           = mock(Key.class);
		
		mockStatic(Key.class);
		when(Key.getKeyById(anyString())).thenReturn(Optional.of(key));
		
		this.authenticationService.authenticate(someKeyString);
		assertTrue(this.authenticationService.getAuthenticatedKey().isPresent());
		assertSame(key, this.authenticationService.getAuthenticatedKey().get());
	}
	
	@Test(expected = NotAuthenticatedException.class)
	public void getAuthenticatedUserViaWrongString() throws Exception {
		mockStatic(Key.class);
		when(Key.getKeyById(anyString())).thenReturn(Optional.empty());
		
		final AuthenticationService authenticationService = this.authenticationService;
		authenticationService.authenticate("This is a non existing Key");
	}
	
	@Test
	public void getAuthenticatedUser() throws Exception {
		final User user = mock(User.class);
		final Key  key  = mock(Key.class);
		
		when(user.getPrimaryKey()).thenReturn(key);
		when(key.isPrimaryKey()).thenReturn(true);
		when(key.getUser()).thenReturn(user);
		
		this.authenticationService.authenticate(user.getPrimaryKey());
		assertTrue(this.authenticationService.getAuthenticatedKey().isPresent());
		assertSame(user, this.authenticationService.getAuthenticatedUser().orElseThrow(AssertionError::new));
	}
}