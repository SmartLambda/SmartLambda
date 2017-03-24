package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.concurrent.ThreadManager;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.torpedoquery.jpa.Torpedo;

import java.util.Calendar;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Application.class, Torpedo.class})
public class ScheduleManagerTest {
	
	private Event  event;
	private String executed;
	private String updatedLock;
	private String save;
	private Event  saveEvent;
	
	@Before
	public void setUp() {
		event = Mockito.mock(Event.class);
		final ListenableFuture future = Mockito.mock(ListenableFuture.class);
		Mockito.when(event.execute()).thenAnswer(invocation -> {
			executed = "Executed";
			return future;
		});
		Mockito.when(future.isDone()).thenReturn(false).thenReturn(true);
		
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
	
	@Test(timeout = 10000)
	public void executeTest() throws Exception {
		ThreadManager.getExecutorService().submit(ScheduleManager.getInstance()::run);
		while (executed == null) ;
		
		Assert.assertEquals("Executed", executed);
	}
	
	@Test(timeout = 10000)
	public void updateLockTest() throws Exception {
		Mockito.doNothing().doAnswer(invocation -> updatedLock = "updatedLock").when(event).setLock(any(Calendar.class));
		ThreadManager.getExecutorService().submit(ScheduleManager.getInstance()::run);
		while (updatedLock == null) ;
		
		Assert.assertEquals("updatedLock", updatedLock);
	}
	
	@Test(timeout = 10000)
	public void finishFutureTest() throws Exception {
		Mockito.doAnswer(invocation -> save = "save").when(event).save();
		ThreadManager.getExecutorService().submit(ScheduleManager.getInstance()::run);
		while (save == null) ;
		
		Assert.assertEquals("save", save);
	}
	
	@After
	public void tearDown() {
		executed = null;
		updatedLock = null;
	}
}