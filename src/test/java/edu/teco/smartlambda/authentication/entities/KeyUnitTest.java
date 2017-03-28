package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.lambda.Lambda;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.torpedoquery.jpa.OnGoingComparableCondition;
import org.torpedoquery.jpa.OnGoingLogicalCondition;
import org.torpedoquery.jpa.Query;
import org.torpedoquery.jpa.Torpedo;
import org.torpedoquery.jpa.ValueOnGoingCondition;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 *
 */
@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Torpedo.class, Application.class, AuthenticationService.class})
public class KeyUnitTest {
	
	private User    user;
	private Lambda  lambda;
	private Key     key;
	private Session session;
	
	@Before
	public void setUp() throws Exception {
		this.user = mock(User.class);
		this.lambda = mock(Lambda.class);
		this.key = spy(Key.class);
		
		when(lambda.getOwner()).thenReturn(user);
		when(user.getPrimaryKey()).thenReturn(Mockito.mock(Key.class));
		
		mockStatic(Torpedo.class);
		mockStatic(Application.class);
		mockStatic(AuthenticationService.class);
		
		this.session = mock(Session.class);
		
		when(Application.getInstance()).thenReturn(mock(Application.class));
		when(Application.getInstance().getSessionFactory()).thenReturn(mock(SessionFactory.class));
		when(Application.getInstance().getSessionFactory().getCurrentSession()).thenReturn(this.session);
		
		when(this.session.get(anyString(), any())).thenReturn(null);
		when(this.session.get(any(Class.class), any())).thenReturn(null);
		
		when(Torpedo.from(any())).thenCallRealMethod();
		when(Torpedo.select(any(Object.class))).thenReturn(mock(Query.class));
		
		when(AuthenticationService.getInstance()).thenReturn(mock(AuthenticationService.class));
		when(AuthenticationService.getInstance().getAuthenticatedKey()).thenReturn(Optional.of(this.key));
		when(AuthenticationService.getInstance().getAuthenticatedUser()).thenReturn(Optional.of(this.user));
	}
	
	@Test
	public void hasPermissionsTest() throws Exception {
		final PermissionType permissionType = PermissionType.CREATE;
		
		final ValueOnGoingCondition      keyCondition;
		final ValueOnGoingCondition      lambdaCondition;
		final OnGoingComparableCondition permissionCondition;
		
		final OnGoingLogicalCondition keyReturnCondition    = mock(OnGoingLogicalCondition.class);
		final OnGoingLogicalCondition lambdaReturnCondition = mock(OnGoingLogicalCondition.class);
		
		doReturn(true).when(key).hasPermission(user, permissionType);
		
		//mock of hasPermission(Lambda, PermissionType) which is the method to test
		when(Torpedo.where(nullable(Key.class))).thenReturn(
				keyCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> keyReturnCondition)));
		when(keyReturnCondition.and(nullable(Lambda.class))).thenReturn(
				lambdaCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> lambdaReturnCondition)));
		when(lambdaReturnCondition.and(nullable(PermissionType.class)))
				.thenReturn(permissionCondition = mock(OnGoingComparableCondition.class));
		
		this.key.hasPermission(lambda, permissionType);
		
		verify(keyCondition).eq(this.key);
		verify(lambdaCondition).eq(this.lambda);
		verify(permissionCondition).eq(permissionType);
	}
	
	@Test
	public void hasUserPermissionsTest() throws Exception {
		final PermissionType permissionType = PermissionType.CREATE;
		
		final ValueOnGoingCondition      keyCondition;
		final ValueOnGoingCondition      userCondition;
		final OnGoingComparableCondition permissionCondition;
		
		final OnGoingLogicalCondition keyReturnCondition    = mock(OnGoingLogicalCondition.class);
		final OnGoingLogicalCondition lambdaReturnCondition = mock(OnGoingLogicalCondition.class);
		
		when(Torpedo.where(nullable(Key.class))).thenReturn(
				keyCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> keyReturnCondition)));
		when(keyReturnCondition.and(nullable(User.class))).thenReturn(
				userCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> lambdaReturnCondition)));
		when(lambdaReturnCondition.and(nullable(PermissionType.class)))
				.thenReturn(permissionCondition = mock(OnGoingComparableCondition.class));
		
		when(Torpedo.select(new Object()).list(session)).thenReturn(new LinkedList<>());
		
		this.key.hasPermission(user, permissionType);
		
		verify(keyCondition).eq(this.key);
		verify(userCondition).eq(this.user);
		verify(permissionCondition).eq(permissionType);
	}
	
	@Test
	public void deleteTest() throws Exception {
		Key testKey = new Key("", "", user);
		when(AuthenticationService.getInstance().getAuthenticatedKey()).thenReturn(Optional.of(testKey));
		final Key[]  deleteKey  = new Key[1];
		final User[] deleteUser = new User[1];
		
		when(this.user.getPrimaryKey()).thenReturn(testKey);
		doAnswer(invocation -> deleteKey[0] = invocation.getArgument(0)).when(session).delete(any(Key.class));
		doAnswer(invocation -> deleteUser[0] = invocation.getArgument(0)).when(session).delete(any(User.class));
		
		testKey.delete();
		
		Assert.assertEquals(user, deleteUser[0]);
		Assert.assertEquals(testKey, deleteKey[0]);
	}
	
	@Test
	public void grantLambdaPermissionTest() throws Exception {
		PermissionType permissionType     = PermissionType.CREATE;
		Permission[]   permission         = new Permission[1];
		Permission     expectedPermission = new Permission(lambda, permissionType, key);
		when(user.getPrimaryKey()).thenReturn(key);
		
		doReturn(false).when(key).hasPermission(lambda, permissionType);
		when(session.save(nullable(Permission.class))).thenAnswer(invocation -> {
			permission[0] = invocation.getArgument(0);
			return null;
		});
		
		key.grantPermission(lambda, permissionType);
		
		Assert.assertTrue(expectedPermission.getKey().equals(permission[0].getKey()) &&
				expectedPermission.getLambda().equals(permission[0].getLambda()) &&
				expectedPermission.getPermissionType().equals(permission[0].getPermissionType()));
	}
	
	@Test
	public void grantUserPermissionTest() throws Exception {
		PermissionType permissionType     = PermissionType.CREATE;
		Permission[]   permission         = new Permission[1];
		Permission     expectedPermission = new Permission(user, permissionType, key);
		when(user.getPrimaryKey()).thenReturn(key);
		
		doReturn(false).when(key).hasPermission(user, permissionType);
		when(session.save(nullable(Permission.class))).thenAnswer(invocation -> {
			permission[0] = invocation.getArgument(0);
			return null;
		});
		
		key.grantPermission(user, permissionType);
		
		Assert.assertTrue(expectedPermission.getKey().equals(permission[0].getKey()) &&
				expectedPermission.getUser().equals(permission[0].getUser()) &&
				expectedPermission.getPermissionType().equals(permission[0].getPermissionType()));
	}
	
	@Test
	public void revokeLambdaPermissionTest() throws Exception {
		when(user.getPrimaryKey()).thenReturn(key);
		
		final boolean[]            deleted        = new boolean[1];
		PermissionType             permissionType = PermissionType.CREATE;
		ValueOnGoingCondition      keyCondition;
		ValueOnGoingCondition      lambdaCondition;
		OnGoingComparableCondition permissionTypeCondition;
		
		OnGoingLogicalCondition keyReturnCondition    = mock(OnGoingLogicalCondition.class);
		OnGoingLogicalCondition lambdaReturnCondition = mock(OnGoingLogicalCondition.class);
		
		when(Torpedo.where(nullable(Key.class))).thenReturn(
				keyCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> keyReturnCondition)));
		when(keyReturnCondition.and(nullable(Lambda.class))).thenReturn(
				lambdaCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> lambdaReturnCondition)));
		when(lambdaReturnCondition.and(nullable(PermissionType.class)))
				.thenReturn(permissionTypeCondition = mock(OnGoingComparableCondition.class));
		
		List<Object> permissions = new LinkedList<>();
		permissions.add(new Permission(user, permissionType, key));
		
		when(Torpedo.select(new Object()).list(session)).thenReturn(permissions);
		doAnswer(invocation -> deleted[0] = true).when(session).delete(any(Permission.class));
		
		key.revokePermission(lambda, permissionType);
		
		verify(keyCondition).eq(key);
		verify(lambdaCondition).eq(lambda);
		verify(permissionTypeCondition).eq(permissionType);
		Assert.assertEquals(true, deleted[0]);
	}
	
	@Test
	public void revokeUserPermissionTest() throws Exception {
		when(user.getPrimaryKey()).thenReturn(key);
		
		final boolean[]            deleted        = new boolean[1];
		PermissionType             permissionType = PermissionType.CREATE;
		ValueOnGoingCondition      keyCondition;
		ValueOnGoingCondition      userCondition;
		OnGoingComparableCondition permissionTypeCondition;
		
		OnGoingLogicalCondition keyReturnCondition  = mock(OnGoingLogicalCondition.class);
		OnGoingLogicalCondition userReturnCondition = mock(OnGoingLogicalCondition.class);
		
		when(Torpedo.where(nullable(Key.class))).thenReturn(
				keyCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> keyReturnCondition)));
		when(keyReturnCondition.and(nullable(User.class))).thenReturn(
				userCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> userReturnCondition)));
		when(userReturnCondition.and(nullable(PermissionType.class)))
				.thenReturn(permissionTypeCondition = mock(OnGoingComparableCondition.class));
		
		List<Object> permissions = new LinkedList<>();
		permissions.add(new Permission(user, permissionType, key));
		
		when(Torpedo.select(new Object()).list(session)).thenReturn(permissions);
		doAnswer(invocation -> deleted[0] = true).when(session).delete(any(Permission.class));
		
		key.revokePermission(user, permissionType);
		
		verify(keyCondition).eq(key);
		verify(userCondition).eq(user);
		verify(permissionTypeCondition).eq(permissionType);
		Assert.assertEquals(true, deleted[0]);
	}
}