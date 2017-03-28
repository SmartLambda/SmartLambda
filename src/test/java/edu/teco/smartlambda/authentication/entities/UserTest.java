package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.torpedoquery.jpa.OnGoingLogicalCondition;
import org.torpedoquery.jpa.OnGoingStringCondition;
import org.torpedoquery.jpa.Query;
import org.torpedoquery.jpa.Torpedo;
import org.torpedoquery.jpa.ValueOnGoingCondition;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 *
 */
@SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({Torpedo.class, Application.class, AuthenticationService.class})
public class UserTest {
	
	private User    user;
	private Key     primaryKey;
	private Session session;
	
	@Before
	public void setUp() throws Exception {
		this.user = mock(User.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));
		this.primaryKey = mock(Key.class);
		
		when(this.primaryKey.isPrimaryKey()).thenReturn(true);
		when(this.user.getPrimaryKey()).thenReturn(this.primaryKey);
		
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
		when(AuthenticationService.getInstance().getAuthenticatedKey()).thenReturn(Optional.of(this.primaryKey));
		when(AuthenticationService.getInstance().getAuthenticatedUser()).thenReturn(Optional.of(this.user));
	}
	
	@Test
	public void createUser() throws Exception {
		when(Torpedo.where((String) null)).thenReturn(mock(OnGoingStringCondition.class));
		
		final String name    =
				"Karl-Theodor Maria Nikolaus Johann Jacob Philipp Franz Joseph Sylvester Buhl-Freiherr von und zu Guttenberg";
		final User   newUser = User.createUser(name).getLeft();
		
		assertEquals(name, newUser.getName());
		assertNotNull(newUser.getPrimaryKey());
	}
	
	@Test
	public void setAdmin() throws Exception {
		this.user.setAdmin(true);
		
		assertTrue(this.user.isAdmin());
		verify(this.session).save(this.user);
	}
	
	@Test
	public void createKey() throws Exception {
		when(Torpedo.where((String) null)).thenReturn(mock(OnGoingStringCondition.class));
		
		final Key key = this.user.createKey("r2d2").getLeft();
		verify(this.session).save(key);
	}
	
	@Test
	public void getVisibleUsersAsAdmin() throws Exception {
		final Field adminField = User.class.getDeclaredField("isAdmin");
		adminField.setAccessible(true);
		adminField.set(this.user, true);
		
		final OnGoingStringCondition<String> stringCondition;
		final ValueOnGoingCondition<Object>  objectCondition;
		
		when(Torpedo.where(anyString())).thenReturn(stringCondition = mock(OnGoingStringCondition.class));
		when(Torpedo.where((Object) any())).thenReturn(objectCondition = mock(ValueOnGoingCondition.class));
		
		verifyNoMoreInteractions(stringCondition);
		verifyNoMoreInteractions(objectCondition);
		
		this.user.getVisibleUsers();
	}
	
	@Test
	public void getVisibleUsersAsNonAdmin() throws Exception {
		final Field adminField = User.class.getDeclaredField("isAdmin");
		adminField.setAccessible(true);
		adminField.set(this.user, false);
		
		// mock query for key-obtaining
		final Query<Key>                  keyQuery;
		final ValueOnGoingCondition<User> keyUserCondition;
		when(Torpedo.where(nullable(User.class))).thenReturn(keyUserCondition = mock(ValueOnGoingCondition.class));
		when(Torpedo.select(any(Key.class))).thenReturn(keyQuery = mock(Query.class));
		
		final ValueOnGoingCondition<Key> permissionsOfKeysCondition;
		when(Torpedo.where(nullable(Key.class))).thenReturn(permissionsOfKeysCondition = mock(ValueOnGoingCondition.class));
		
		// mock the query for permission, where users shall be obtained from
		final Query<Permission> permissionQuery;
		when(Torpedo.select(any(Permission.class))).thenReturn(permissionQuery = mock(Query.class));
		
		// generate a set of mocked permissions, that contain different users in different permission targets and verify that the users
		// are obtained from all different targets
		final User lambdaOwner = mock(User.class);
		final User keyOwner    = mock(User.class);
		
		final Lambda lambda = mock(Lambda.class);
		when(lambda.getOwner()).thenReturn(lambdaOwner);
		
		final Permission permissionWithoutAnyForeighners = mock(Permission.class);
		final Permission permissionWithForeighnLambda    = mock(Permission.class);
		final Permission permissionWithForeighnKey       = mock(Permission.class);
		
		when(permissionWithForeighnLambda.getLambda()).thenReturn(lambda);
		when(permissionWithForeighnKey.getUser()).thenReturn(keyOwner);
		
		when(permissionQuery.list(this.session))
				.thenReturn(Arrays.asList(permissionWithoutAnyForeighners, permissionWithForeighnLambda, permissionWithForeighnKey));
		
		// execute the to-test method
		final Set<User> users = this.user.getVisibleUsers();
		
		// verify, that the obtained keys are only of the current user
		verify(keyUserCondition).eq(this.user);
		
		// verify, that the obtained permissions are all of keys, that were obtained in the keyQuery before
		verify(permissionsOfKeysCondition).in(keyQuery);
		
		// verify that the permissions that shall be examined for users are listed
		verify(permissionQuery).list(this.session);
		
		assertEquals(2, users.size());
		assertTrue(users.contains(lambdaOwner));
		assertTrue(users.contains(keyOwner));
	}
	
	@Test
	public void getLambdas() throws Exception {
		final ValueOnGoingCondition userCondition;
		when(Torpedo.where(any(User.class))).thenReturn(userCondition = mock(ValueOnGoingCondition.class));
		
		this.user.getLambdas();
		
		verify(userCondition).eq(this.user);
		verifyNoMoreInteractions(userCondition);
	}
	
	@Test
	public void getVisibleLambdas() throws Exception {
		final Lambda visibleLambda   = mock(Lambda.class);
		final Lambda invisibleLambda = mock(Lambda.class);
		
		doReturn(Arrays.asList(visibleLambda, invisibleLambda)).when(this.user).getLambdas();
		when(this.primaryKey.hasPermission(visibleLambda, PermissionType.READ)).thenReturn(true);
		when(this.primaryKey.hasPermission(invisibleLambda, PermissionType.READ)).thenReturn(false);
		
		final Set<AbstractLambda> visibleLambdas = this.user.getVisibleLambdas();
		assertTrue(visibleLambdas.size() == 1);
		assertTrue(visibleLambdas.iterator().next() == visibleLambda);
	}
	
	@Test
	public void getKeyByName() throws Exception {
		final OnGoingStringCondition nameCondition;
		final ValueOnGoingCondition  userCondition;
		
		final OnGoingLogicalCondition userReturnCondition = mock(OnGoingLogicalCondition.class);
		
		when(Torpedo.where(any(User.class))).thenReturn(
				userCondition = mock(ValueOnGoingCondition.class, withSettings().defaultAnswer(invocation -> userReturnCondition)));
		
		when(userReturnCondition.and((String) null)).thenReturn(nameCondition = mock(OnGoingStringCondition.class));
		
		this.user.getKeyByName("Normandy");
		
		verify(nameCondition).eq("Normandy");
		verify(userCondition).eq(this.user);
		verifyNoMoreInteractions(nameCondition);
		verifyNoMoreInteractions(userCondition);
	}
	
	@Test
	public void getByName() throws Exception {
		final OnGoingStringCondition condition;
		when(Torpedo.where((String) null)).thenReturn(condition = mock(OnGoingStringCondition.class));
		User.getByName("mordin.solus");
		
		verify(condition).eq("mordin.solus");
		verifyNoMoreInteractions(condition);
	}
	
	@Test
	public void getId() throws Exception {
		this.user.getId();
	}
	
	@Test
	public void getName() throws Exception {
		this.user.getName();
	}
	
	@Test
	public void getPrimaryKey() throws Exception {
		this.user.getPrimaryKey();
	}
	
	@Test
	public void isAdmin() throws Exception {
		this.user.isAdmin();
	}
}