package edu.teco.smartlambda.rest.controller;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.utility.TestUtility;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import spark.Request;
import spark.Response;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(AuthenticationService.class)
public class KeyControllerTest {
	private AuthenticationService authenticationService;
	private Request               request;
	private Response              response;
	private User                  user;
	
	private static final String REQUEST_KEY_NAME = "TestKeyName";
	private static final String KEY_HASH         = "abc";
	
	@Before
	public void setUp() {
		this.user = mock(User.class);
		
		PowerMockito.mockStatic(AuthenticationService.class);
		this.authenticationService = mock(AuthenticationService.class);
		when(AuthenticationService.getInstance()).thenReturn(this.authenticationService);
		when(this.authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(this.user));
		
		this.request = mock(Request.class);
		when(this.request.params(":name")).thenReturn(REQUEST_KEY_NAME);
		
		this.response = mock(Response.class);
	}
	
	@Test
	public void testPrivateConstructor() throws Exception {
		TestUtility.coverPrivateDefaultConstructor(KeyController.class);
	}
	
	@Test
	public void createKey() throws Exception {
		final Key key = mock(Key.class);
		
		when(this.user.createKey(REQUEST_KEY_NAME)).thenReturn(Pair.of(key, KEY_HASH));
		
		assertEquals(KEY_HASH, KeyController.createKey(this.request, this.response));
		verify(this.response).status(201);
	}
	
	@Test
	public void deleteKey() throws Exception {
		final Key key = mock(Key.class);
		
		when(this.user.getKeyByName(REQUEST_KEY_NAME)).thenReturn(Optional.of(key));
		assertSame(Object.class, KeyController.deleteKey(this.request, this.response).getClass());
		verify(key).delete();
		verify(this.response).status(200);
	}
}