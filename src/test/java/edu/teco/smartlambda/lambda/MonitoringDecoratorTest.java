package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.concurrent.ThreadManager;
import edu.teco.smartlambda.monitoring.MonitoringService;
import edu.teco.smartlambda.runtime.ExecutionResult;
import edu.teco.smartlambda.schedule.Event;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MonitoringService.class, Application.class})
public class MonitoringDecoratorTest {
	
	private MonitoringDecorator monitoredLambda;
	
	private AbstractLambda    innerLambda;
	private MonitoringService mockedMonitoringService;
	
	@Before
	public void setUp() {
		this.innerLambda = mock(AbstractLambda.class);
		this.monitoredLambda = new MonitoringDecorator(this.innerLambda);
		
		this.mockedMonitoringService = mock(MonitoringService.class);
		mockStatic(MonitoringService.class);
		when(MonitoringService.getInstance()).thenReturn(this.mockedMonitoringService);
	}
	
	@Test
	public void executeSync() throws Exception {
		final ExecutionResult      mockedExecutionResult      = mock(ExecutionResult.class);
		final ExecutionReturnValue mockedExecutionReturnValue = mock(ExecutionReturnValue.class);
		
		when(this.innerLambda.executeSync("")).thenReturn(mockedExecutionResult);
		when(mockedExecutionResult.getConsumedCPUTime()).thenReturn(1L);
		when(mockedExecutionResult.getExecutionReturnValue()).thenReturn(mockedExecutionReturnValue);
		
		this.monitoredLambda.executeSync("");
		verify(this.mockedMonitoringService).onLambdaExecutionStart(this.innerLambda);
		verify(this.mockedMonitoringService).onLambdaExecutionEnd(this.innerLambda, 1L, mockedExecutionReturnValue,
				/* monitoring service is mocked, therefore no event exists */ null);
		
		verify(this.innerLambda).executeSync("");
	}
	
	@Test
	public void executeAsync() throws Exception {
		final ExecutionResult      mockedExecutionResult      = mock(ExecutionResult.class);
		final ExecutionReturnValue mockedExecutionReturnValue = mock(ExecutionReturnValue.class);
		
		mockStatic(Application.class);
		final Application    application = mock(Application.class);
		final SessionFactory factory     = mock(SessionFactory.class);
		final Session        session     = mock(Session.class);
		
		when(Application.getInstance()).thenReturn(application);
		when(application.getSessionFactory()).thenReturn(factory);
		when(factory.getCurrentSession()).thenReturn(session);
		when(session.getTransaction()).thenReturn(mock(Transaction.class));
		
		// test success
		when(this.innerLambda.executeAsync("")).thenReturn(ThreadManager.getExecutorService().submit(() -> mockedExecutionResult));
		when(mockedExecutionResult.getConsumedCPUTime()).thenReturn(1L);
		when(mockedExecutionResult.getExecutionReturnValue()).thenReturn(mockedExecutionReturnValue);
		this.monitoredLambda.executeAsync("").get();
		verify(this.mockedMonitoringService, times(1)).onLambdaExecutionEnd(this.innerLambda, 1L, mockedExecutionReturnValue, null);
		
		// test failure
		when(this.innerLambda.executeAsync("")).thenReturn(ThreadManager.getExecutorService().submit(() -> {throw new Exception();}));
		try {
			this.monitoredLambda.executeAsync("").get();
			fail();
		} catch (final ExecutionException e) {
		}
		
		verify(this.mockedMonitoringService, times(2)).onLambdaExecutionStart(this.innerLambda);
		verify(this.innerLambda, times(2)).executeAsync("");
		verify(this.mockedMonitoringService, times(1))
				.onLambdaExecutionEnd(eq(this.innerLambda), eq(0L), any(ExecutionReturnValue.class), isNull());
	}
	
	@Test
	public void save() throws Exception {
		this.monitoredLambda.save();
		verify(this.mockedMonitoringService).onLambdaDeployment(this.innerLambda);
		verify(this.innerLambda).save();
	}
	
	@Test
	public void update() throws Exception {
		this.monitoredLambda.update();
		verify(this.innerLambda).update();
	}
	
	@Test
	public void delete() throws Exception {
		this.monitoredLambda.delete();
		verify(this.mockedMonitoringService).onLambdaDeletion(this.innerLambda);
		verify(this.innerLambda).delete();
	}
	
	@Test
	public void schedule() throws Exception {
		final Event event = mock(Event.class);
		this.monitoredLambda.schedule(event);
		verify(this.innerLambda).schedule(event);
	}
	
	@Test
	public void deployBinary() throws Exception {
		final byte[] bytes = new byte[10];
		this.monitoredLambda.deployBinary(bytes);
		verify(this.innerLambda).deployBinary(bytes);
	}
	
	@Test
	public void getScheduledEvent() throws Exception {
		this.monitoredLambda.getScheduledEvent("sucheventmuchwow");
		verify(this.innerLambda).getScheduledEvent("sucheventmuchwow");
	}
	
	@Test
	public void getScheduledEvents() throws Exception {
		this.monitoredLambda.getScheduledEvents();
		verify(this.innerLambda).getScheduledEvents();
	}
	
	@Test
	public void getMonitoringEvents() throws Exception {
		this.monitoredLambda.getMonitoringEvents();
		verify(this.innerLambda).getMonitoringEvents();
	}
}