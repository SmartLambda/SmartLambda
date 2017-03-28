package edu.teco.smartlambda.authentication.entities;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.lambda.Lambda;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
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
	public void getPermissionsTest() throws Exception {
		when(Torpedo.where(any(Key.class)));
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
}