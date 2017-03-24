package edu.teco.smartlambda.rest.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.identity.IdentityProvider;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import spark.Request;
import spark.Response;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthenticationService.class, IdentityProviderRegistry.class})
public class UserControllerTest {
	private static final String TEST_IDENTITY_PROVIDER_NAME = "TestIdentityProvider";
	
	private User             testUser;
	private IdentityProvider testIdentityProvider;
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(AuthenticationService.class);
		
		this.testUser = mock(User.class);
		
		final AuthenticationService authenticationService = mock(AuthenticationService.class);
		when(AuthenticationService.getInstance()).thenReturn(authenticationService);
		when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(this.testUser));
		
		PowerMockito.mockStatic(IdentityProviderRegistry.class);
		final IdentityProviderRegistry registry = mock(IdentityProviderRegistry.class);
		when(IdentityProviderRegistry.getInstance()).thenReturn(registry);
		
		this.testIdentityProvider = mock(IdentityProvider.class);
		when(registry.getIdentityProviderByName(TEST_IDENTITY_PROVIDER_NAME)).thenReturn(Optional.of(this.testIdentityProvider));
	}
	
	@Test
	public void getUserList() throws Exception {
		final Set<User> userList = new HashSet<>();
		
		User user = mock(User.class);
		when(user.getName()).thenReturn("TestUser1");
		userList.add(user);
		
		user = mock(User.class);
		when(user.getName()).thenReturn("TestUser2");
		userList.add(user);
		
		user = mock(User.class);
		when(user.getName()).thenReturn("TestUser3");
		userList.add(user);
		
		when(this.testUser.getVisibleUsers()).thenReturn(userList);
		
		final Request  request  = mock(Request.class);
		final Response response = mock(Response.class);
		
		final Object object = UserController.getUserList(request, response);
		assertTrue(object instanceof Collection);
		
		final Collection collection = (Collection) object;
		assertEquals(3, collection.size());
		assertTrue(collection.contains("TestUser1"));
		assertTrue(collection.contains("TestUser2"));
		assertTrue(collection.contains("TestUser3"));
		
		verify(response).status(200);
	}
	
	@Test
	public void register() throws Exception {
		final JsonObject json       = new JsonObject();
		final JsonObject parameters = new JsonObject();
		json.addProperty("identityProvider", TEST_IDENTITY_PROVIDER_NAME);
		json.add("parameters", parameters);
		parameters.addProperty("TestParameter", "TestParameterValue");
		
		final Request  request  = mock(Request.class);
		final Response response = mock(Response.class);
		
		when(request.body()).thenReturn(new Gson().toJson(json));
		
		final User user = mock(User.class);
		when(user.getName()).thenReturn("TestUserName");
		
		when(this.testIdentityProvider.register(any())).then((InvocationOnMock invocation) -> {
			final Map<String, String> invocationArgument = invocation.getArgument(0);
			assertEquals("TestParameterValue", invocationArgument.get("TestParameter"));
			return new ImmutablePair<User, String>(user, "123456789");
		});
		
		final Object object     = UserController.register(request, response);
		final Field  name       = object.getClass().getDeclaredField("name");
		final Field  primaryKey = object.getClass().getDeclaredField("primaryKey");
		
		assertNotNull(name);
		assertNotNull(primaryKey);
		name.setAccessible(true);
		primaryKey.setAccessible(true);
		assertEquals("TestUserName", name.get(object));
		assertEquals("123456789", primaryKey.get(object));
		
		verify(response).status(201);
	}
}
