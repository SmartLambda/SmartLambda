package edu.teco.smartlambda.rest.controller;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.Permission;
import edu.teco.smartlambda.authentication.entities.PermissionType;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({User.class, AuthenticationService.class, LambdaFacade.class})
public class PermissionControllerTest {
	private static final String TEST_USER_1_NAME   = "TestUser1";
	private static final String TEST_USER_2_NAME   = "TestUser2";
	private static final String TEST_USER_3_NAME   = "TestUser3";
	private static final String TEST_LAMBDA_1_NAME = "TestLambda1";
	private static final String TEST_LAMBDA_2_NAME = "TestLambda2";
	
	private User   testUser1;
	private User   testUser2;
	private User   testUser3;
	private Key    testKey;
	private Lambda testLambda1;
	private Lambda testLambda2;
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(User.class);
		PowerMockito.mockStatic(AuthenticationService.class);
		PowerMockito.mockStatic(LambdaFacade.class);
		
		this.testUser1 = mock(User.class);
		this.testUser2 = mock(User.class);
		this.testUser3 = mock(User.class);
		when(User.getByName(TEST_USER_1_NAME)).thenReturn(Optional.of(this.testUser1));
		
		when(this.testUser1.getName()).thenReturn(TEST_USER_1_NAME);
		when(this.testUser2.getName()).thenReturn(TEST_USER_2_NAME);
		when(this.testUser3.getName()).thenReturn(TEST_USER_3_NAME);
		
		this.testKey = mock(Key.class);
		when(this.testKey.isPrimaryKey()).thenReturn(true);
		when(this.testUser1.getPrimaryKey()).thenReturn(this.testKey);
		
		this.testLambda1 = mock(Lambda.class);
		this.testLambda2 = mock(Lambda.class);
		when(this.testLambda1.getName()).thenReturn(TEST_LAMBDA_1_NAME);
		when(this.testLambda2.getName()).thenReturn(TEST_LAMBDA_2_NAME);
		when(this.testLambda1.getOwner()).thenReturn(this.testUser2);
		when(this.testLambda2.getOwner()).thenReturn(this.testUser2);
	}
	
	private void verifyPermissionCollection(final Collection collection, final String expectedUser, final String expectedName)
			throws Exception {
		assertEquals(1, collection.size());
		
		final Object perm = collection.iterator().next();
		final Field  user = perm.getClass().getDeclaredField("user");
		final Field  name = perm.getClass().getDeclaredField("name");
		assertSame(String.class, user.getType());
		assertSame(String.class, name.getType());
		assertEquals(2, perm.getClass().getDeclaredFields().length);
		
		user.setAccessible(true);
		name.setAccessible(true);
		assertEquals(expectedUser, user.get(perm));
		assertEquals(expectedName, name.get(perm));
	}
	
	@Test
	public void readUserPermissions() throws Exception {
		final Set<Permission> permissions = new HashSet<>();
		Permission            permission  = mock(Permission.class);
		when(permission.getLambda()).thenReturn(this.testLambda1);
		when(permission.getPermissionType()).thenReturn(PermissionType.EXECUTE);
		permissions.add(permission);
		
		permission = mock(Permission.class);
		when(permission.getLambda()).thenReturn(this.testLambda2);
		when(permission.getPermissionType()).thenReturn(PermissionType.READ);
		permissions.add(permission);
		
		permission = mock(Permission.class);
		when(permission.getUser()).thenReturn(this.testUser3);
		when(permission.getPermissionType()).thenReturn(PermissionType.GRANT);
		permissions.add(permission);
		
		when(this.testKey.getVisiblePermissions()).thenReturn(permissions);
		
		final Request  request  = mock(Request.class);
		final Response response = mock(Response.class);
		when(request.params(":user")).thenReturn(TEST_USER_1_NAME);
		
		final Object result = PermissionController.readUserPermissions(request, response);
		assertTrue(result instanceof Map);
		final Map map = (Map) result;
		
		//noinspection unchecked
		map.forEach((key, value) -> {
			try {
				assertTrue(value instanceof Collection);
				assertTrue(key instanceof String);
				
				switch (PermissionType.valueOf(((String) key).toUpperCase())) {
					case EXECUTE:
						this.verifyPermissionCollection((Collection) value, TEST_USER_2_NAME, TEST_LAMBDA_1_NAME);
						break;
					case READ:
						this.verifyPermissionCollection((Collection) value, TEST_USER_2_NAME, TEST_LAMBDA_2_NAME);
						break;
					case GRANT:
						this.verifyPermissionCollection((Collection) value, TEST_USER_3_NAME, "*");
						break;
					default:
						assertEquals(0, ((Collection) value).size());
				}
			} catch (final Exception e) {
				throw new RuntimeException(e);
			}
		});
		
		verify(response).status(200);
	}
	
	@Test
	public void grantUserPermissions() throws Exception {
	}
	
	@Test
	public void revokeUserPermissions() throws Exception {
	}
	
	@Test
	public void readKeyPermissions() throws Exception {
	}
	
	@Test
	public void grantKeyPermissions() throws Exception {
	}
	
	@Test
	public void revokeKeyPermissions() throws Exception {
	}
}