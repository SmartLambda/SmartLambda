package edu.teco.smartlambda.rest.controller;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import spark.Request;
import spark.Response;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthenticationService.class, IdentityProviderRegistry.class})
public class UserControllerTest {
	private User testUser;
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(AuthenticationService.class);
		
		this.testUser = mock(User.class);
		
		final AuthenticationService authenticationService = mock(AuthenticationService.class);
		when(AuthenticationService.getInstance()).thenReturn(authenticationService);
		when(authenticationService.getAuthenticatedUser()).thenReturn(Optional.of(this.testUser));
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
	}
	
	@Test
	public void register() throws Exception {
		
	}
}