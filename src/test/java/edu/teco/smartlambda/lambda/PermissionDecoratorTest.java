package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.PermissionType;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.schedule.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthenticationService.class, LambdaDecorator.class})
public class PermissionDecoratorTest {
	
	private PermissionDecorator   lambda;
	private AbstractLambda        innerLambda;
	private Lambda                unwrappedLambda;
	private AuthenticationService mockedAuthenticationService;
	
	private Key mockedKey;
	
	@Before
	public void setup() {
		mockStatic(AuthenticationService.class);
		when(AuthenticationService.getInstance()).thenReturn(this.mockedAuthenticationService = mock(AuthenticationService.class));
		
		when(this.mockedAuthenticationService.getAuthenticatedKey()).thenReturn(Optional.of(this.mockedKey = mock(Key.class)));
		
		this.innerLambda = mock(AbstractLambda.class);
		this.lambda = new PermissionDecorator(this.innerLambda);
		
		mockStatic(LambdaDecorator.class);
		when(LambdaDecorator.unwrap(this.innerLambda)).thenReturn(this.unwrappedLambda = mock(Lambda.class));
		
		when(this.mockedKey.hasPermission(eq(this.unwrappedLambda), any(PermissionType.class))).thenReturn(true);
	}
	
	@Test
	public void executeSync() throws Exception {
		this.lambda.executeSync("");
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.EXECUTE);
		verify(this.innerLambda).executeSync("");
	}
	
	@Test
	public void executeAsync() throws Exception {
		this.lambda.executeAsync("");
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.EXECUTE);
		verify(this.innerLambda).executeAsync("");
	}
	
	@Test
	public void save() throws Exception {
		this.lambda.save();
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.CREATE);
		verify(this.innerLambda).save();
	}
	
	@Test
	public void update() throws Exception {
		this.lambda.update();
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.PATCH);
		verify(this.innerLambda).update();
	}
	
	@Test
	public void delete() throws Exception {
		this.lambda.delete();
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.DELETE);
		verify(this.innerLambda).delete();
	}
	
	@Test
	public void schedule() throws Exception {
		final Event event = mock(Event.class);
		this.lambda.schedule(event);
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.SCHEDULE);
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.EXECUTE);
		verify(this.innerLambda).schedule(event);
	}
	
	@Test
	public void deployBinary() throws Exception {
		final byte[] arr = new byte[1];
		this.lambda.deployBinary(arr);
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.CREATE);
		verify(this.innerLambda).deployBinary(arr);
	}
	
	@Test
	public void patchBinary() throws Exception {
		// if a lambda got an id, that is not 0, it already exists and therefore is being patched, if a new binary is deployed
		when(this.unwrappedLambda.getId()).thenReturn(1337);
		
		final byte[] arr = new byte[1];
		this.lambda.deployBinary(arr);
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.PATCH);
		verify(this.innerLambda).deployBinary(arr);
	}
	
	@Test
	public void getScheduledEvent() throws Exception {
		final Event event = mock(Event.class);
		when(this.innerLambda.getScheduledEvent("test")).thenReturn(Optional.of(event));
		when(event.getKey()).thenReturn(this.mockedKey);
		
		final User mockedUser;
		when(this.mockedAuthenticationService.getAuthenticatedUser()).thenReturn(Optional.of(mockedUser = mock(User.class)));
		when(mockedUser.getPrimaryKey()).thenReturn(this.mockedKey);
		when(this.mockedKey.getUser()).thenReturn(mockedUser);
		when(this.mockedKey.isPrimaryKey()).thenReturn(true);
		
		//noinspection OptionalGetWithoutIsPresent
		assert this.lambda.getScheduledEvent("test").get() == event;
		
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.SCHEDULE);
		verify(this.innerLambda).getScheduledEvent("test");
	}
	
	/**
	 * If a user requests a scheduled event that was not created by himself, he additionally needs the read permission
	 *
	 * @throws Exception on any failure
	 */
	@Test
	public void getScheduledEventAsForeighner() throws Exception {
		final User mockedUser;
		when(this.mockedAuthenticationService.getAuthenticatedUser()).thenReturn(Optional.of(mockedUser = mock(User.class)));
		when(mockedUser.getPrimaryKey()).thenReturn(this.mockedKey);
		
		final Event event       = mock(Event.class);
		final Key   anotherKey  = mock(Key.class);
		final User  anotherUser = mock(User.class);
		
		when(this.innerLambda.getScheduledEvent("test")).thenReturn(Optional.of(event));
		when(event.getKey()).thenReturn(anotherKey);
		when(anotherKey.getUser()).thenReturn(anotherUser);
		when(anotherKey.isPrimaryKey()).thenReturn(false);
		
		//noinspection OptionalGetWithoutIsPresent
		assert this.lambda.getScheduledEvent("test").get() == event;
		
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.SCHEDULE);
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.READ);
		verify(this.innerLambda).getScheduledEvent("test");
	}
	
	@Test
	public void getScheduledEvents() throws Exception {
		this.lambda.getScheduledEvents();
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.SCHEDULE);
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.READ);
		verify(this.innerLambda).getScheduledEvents();
	}
	
	@Test
	public void getMonitoringEvents() throws Exception {
		this.lambda.getMonitoringEvents();
		verify(this.mockedKey).hasPermission(this.unwrappedLambda, PermissionType.STATUS);
		verify(this.innerLambda).getMonitoringEvents();
	}
}