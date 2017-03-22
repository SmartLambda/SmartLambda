package edu.teco.smartlambda.schedule;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.concurrent.ThreadManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.torpedoquery.jpa.Torpedo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Application.class, Torpedo.class})
public class ScheduleManagerTest {
	
	Event  event;
	String response;
	Event  saveEvent;
	
	@Before
	public void setUp() {
		event = Mockito.mock(Event.class);
		Mockito.when(event.execute()).thenAnswer(invocation -> response = "Executed");
		PowerMockito.mockStatic(Application.class);
		PowerMockito.when(Application.getInstance()).thenReturn(Mockito.mock(Application.class));
		PowerMockito.when(Application.getInstance().getSessionFactory()).thenReturn(Mockito.mock(SessionFactory.class));
		Session session = Mockito.mock(Session.class);
		PowerMockito.when(Application.getInstance().getSessionFactory().getCurrentSession()).thenReturn(session);
		Mockito.doAnswer(invocation -> saveEvent = invocation.getArgument(0)).when(session).update(any(Event.class));
		Mockito.doAnswer(invocation -> saveEvent = invocation.getArgument(0)).when(session).saveOrUpdate(any(Event.class));
		Transaction transaction = Mockito.mock(Transaction.class);
		Mockito.when(session.getTransaction()).thenReturn(transaction);
		Mockito.doNothing().when(transaction).commit();
		Mockito.when(session.beginTransaction()).thenReturn(transaction);
		Mockito.when(session.createQuery(anyString())).thenReturn(Mockito.mock(org.hibernate.query.Query.class));
		Mockito.when(session.createQuery(anyString()).getSingleResult()).thenReturn(event);
	}
	
	@Test
	public void executeTest() {
		ThreadManager.getExecutorService().submit(ScheduleManager.getInstance()::run);
		Assert.assertEquals("Executed", response);
		ScheduleManager.getInstance().setRunning(false);
	}
	
}