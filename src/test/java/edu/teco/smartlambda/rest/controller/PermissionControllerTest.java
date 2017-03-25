package edu.teco.smartlambda.rest.controller;

import com.google.gson.Gson;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.Permission;
import edu.teco.smartlambda.authentication.entities.PermissionType;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.lambda.LambdaFactory;
import lombok.Data;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
	private Key    testKey1;
	private Key    testKey3;
	private Lambda testLambda1;
	private Lambda testLambda2;
	
	@Data
	private static class PermissionRequest {
		private PermissionObject[] read;
		private PermissionObject[] patch;
		private PermissionObject[] execute;
		private PermissionObject[] delete;
		private PermissionObject[] status;
		private PermissionObject[] schedule;
		private PermissionObject[] create;
		private PermissionObject[] grant;
	}
	
	@Data
	private static class PermissionObject {
		private String user;
		private String name;
	}
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(User.class);
		PowerMockito.mockStatic(AuthenticationService.class);
		PowerMockito.mockStatic(LambdaFacade.class);
		
		this.testUser1 = mock(User.class);
		this.testUser2 = mock(User.class);
		this.testUser3 = mock(User.class);
		when(User.getByName(TEST_USER_1_NAME)).thenReturn(Optional.of(this.testUser1));
		when(User.getByName(TEST_USER_2_NAME)).thenReturn(Optional.of(this.testUser2));
		when(User.getByName(TEST_USER_3_NAME)).thenReturn(Optional.of(this.testUser3));
		
		when(this.testUser1.getName()).thenReturn(TEST_USER_1_NAME);
		when(this.testUser2.getName()).thenReturn(TEST_USER_2_NAME);
		when(this.testUser3.getName()).thenReturn(TEST_USER_3_NAME);
		
		this.testKey1 = mock(Key.class);
		when(this.testKey1.isPrimaryKey()).thenReturn(true);
		when(this.testUser1.getPrimaryKey()).thenReturn(this.testKey1);
		
		this.testKey3 = mock(Key.class);
		when(this.testKey3.isPrimaryKey()).thenReturn(true);
		when(this.testUser3.getPrimaryKey()).thenReturn(this.testKey3);
		
		this.testLambda1 = mock(Lambda.class);
		this.testLambda2 = mock(Lambda.class);
		when(this.testLambda1.getName()).thenReturn(TEST_LAMBDA_1_NAME);
		when(this.testLambda2.getName()).thenReturn(TEST_LAMBDA_2_NAME);
		when(this.testLambda1.getOwner()).thenReturn(this.testUser2);
		when(this.testLambda2.getOwner()).thenReturn(this.testUser2);
		
		final LambdaFacade lambdaFacade = mock(LambdaFacade.class);
		when(LambdaFacade.getInstance()).thenReturn(lambdaFacade);
		
		final LambdaFactory lambdaFactory = mock(LambdaFactory.class);
		when(lambdaFacade.getFactory()).thenReturn(lambdaFactory);
		when(lambdaFactory.getLambdaByOwnerAndName(this.testUser2, TEST_LAMBDA_1_NAME)).thenReturn(Optional.ofNullable(this.testLambda1));
		when(lambdaFactory.getLambdaByOwnerAndName(this.testUser2, TEST_LAMBDA_2_NAME)).thenReturn(Optional.ofNullable(this.testLambda2));
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
		
		when(this.testKey1.getVisiblePermissions()).thenReturn(permissions);
		
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
		final PermissionRequest permissionRequest = new PermissionRequest();
		
		PermissionObject permissionObject = new PermissionObject();
		permissionObject.setUser(TEST_USER_2_NAME);
		permissionObject.setName(TEST_LAMBDA_1_NAME);
		permissionRequest.setRead(new PermissionObject[] {permissionObject});
		
		permissionObject = new PermissionObject();
		permissionObject.setUser(TEST_USER_2_NAME);
		permissionObject.setName(TEST_LAMBDA_2_NAME);
		permissionRequest.setExecute(new PermissionObject[] {permissionObject});
		
		permissionObject = new PermissionObject();
		permissionObject.setUser(TEST_USER_2_NAME);
		permissionObject.setName("*");
		permissionRequest.setDelete(new PermissionObject[] {permissionObject});
		
		final Request  request  = mock(Request.class);
		final Response response = mock(Response.class);
		
		when(request.body()).thenReturn(new Gson().toJson(permissionRequest));
		when(request.params(":user")).thenReturn(TEST_USER_3_NAME);
		
		final Object result = PermissionController.grantUserPermissions(request, response);
		assertSame(Object.class, result.getClass());
		
		verify(this.testKey3).grantPermission(this.testLambda1, PermissionType.READ);
		verify(this.testKey3).grantPermission(this.testLambda2, PermissionType.EXECUTE);
		verify(this.testKey3).grantPermission(this.testUser2, PermissionType.DELETE);
		verifyNoMoreInteractions(this.testKey3);
		verify(response).status(200);
	}
	
	@Test
	public void revokeUserPermissions() throws Exception {
		final PermissionRequest permissionRequest = new PermissionRequest();
		
		PermissionObject permissionObject = new PermissionObject();
		permissionObject.setUser(TEST_USER_2_NAME);
		permissionObject.setName(TEST_LAMBDA_1_NAME);
		permissionRequest.setRead(new PermissionObject[] {permissionObject});
		
		permissionObject = new PermissionObject();
		permissionObject.setUser(TEST_USER_2_NAME);
		permissionObject.setName(TEST_LAMBDA_2_NAME);
		permissionRequest.setExecute(new PermissionObject[] {permissionObject});
		
		permissionObject = new PermissionObject();
		permissionObject.setUser(TEST_USER_2_NAME);
		permissionObject.setName("*");
		permissionRequest.setDelete(new PermissionObject[] {permissionObject});
		
		final Request  request  = mock(Request.class);
		final Response response = mock(Response.class);
		
		when(request.body()).thenReturn(new Gson().toJson(permissionRequest));
		when(request.params(":user")).thenReturn(TEST_USER_3_NAME);
		
		final Object result = PermissionController.revokeUserPermissions(request, response);
		assertSame(Object.class, result.getClass());
		
		verify(this.testKey3).revokePermission(this.testLambda1, PermissionType.READ);
		verify(this.testKey3).revokePermission(this.testLambda2, PermissionType.EXECUTE);
		verify(this.testKey3).revokePermission(this.testUser2, PermissionType.DELETE);
		verifyNoMoreInteractions(this.testKey3);
		verify(response).status(200);
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