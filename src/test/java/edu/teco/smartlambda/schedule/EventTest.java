package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.lambda.LambdaFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Application.class, AuthenticationService.class, LambdaFacade.class})
public class EventTest {
	
	private Event saveEvent;
	private final Event event = new Event();
	private ListenableFuture future;
	
	@Before
	public void setUp() throws Exception {
		final Key key = Mockito.mock(Key.class);
		this.event.setKey(key);
		this.event.setLambda(Mockito.mock(Lambda.class));
		this.event.setParameters("");
		this.event.setLock(Calendar.getInstance());
		this.event.setNextExecution(Calendar.getInstance());
		this.event.setCronExpression("0 30 10-13 ? * WED,FRI");
		
		PowerMockito.mockStatic(Application.class);
		PowerMockito.when(Application.getInstance()).thenReturn(Mockito.mock(Application.class));
		PowerMockito.when(Application.getInstance().getSessionFactory()).thenReturn(Mockito.mock(SessionFactory.class));
		
		final Session session = Mockito.mock(Session.class);
		PowerMockito.when(Application.getInstance().getSessionFactory().getCurrentSession()).thenReturn(session);
		Mockito.doAnswer(invocation -> this.saveEvent = invocation.getArgument(0)).when(session).saveOrUpdate(any(Event.class));
		
		Mockito.doAnswer(invocation -> this.saveEvent = invocation.getArgument(0)).when(session).delete(any(Event.class));
		
		PowerMockito.mockStatic(AuthenticationService.class);
		final AuthenticationService authService = Mockito.mock(AuthenticationService.class);
		PowerMockito.when(AuthenticationService.getInstance()).thenReturn(authService);
		PowerMockito.doNothing().when(authService).authenticate(key);
		
		this.future = Mockito.mock(ListenableFuture.class);
		
		PowerMockito.mockStatic(LambdaFacade.class);
		Mockito.when(LambdaFacade.getInstance()).thenReturn(Mockito.mock(LambdaFacade.class));
		Mockito.when(LambdaFacade.getInstance().getFactory()).thenReturn(Mockito.mock(LambdaFactory.class));
		final AbstractLambda abstractLambda = Mockito.mock(AbstractLambda.class);
		Mockito.when(LambdaFacade.getInstance().getFactory().decorate(any(Lambda.class))).thenReturn(abstractLambda);
		Mockito.when(abstractLambda.executeAsync(anyString())).thenReturn(this.future);
	}
	
	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void execute() throws Exception {
		this.event.execute();
		Assert.assertTrue(this.future != null);
	}
	
	@Test
	public void save() throws Exception {
		this.event.save();
		Assert.assertEquals(this.event, this.saveEvent);
	}
	
	@Test
	public void delete() throws Exception {
		this.event.delete();
		Assert.assertEquals(this.event, this.saveEvent);
	}
}