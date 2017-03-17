package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.monitoring.MonitoringService;
import edu.teco.smartlambda.runtime.ExecutionResult;
import edu.teco.smartlambda.schedule.Event;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MonitoringService.class, Futures.class})
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
		mockStatic(Futures.class);
		
		this.monitoredLambda.executeAsync("");
		verify(this.mockedMonitoringService).onLambdaExecutionStart(this.innerLambda);
		verify(this.innerLambda).executeAsync("");
		
		final ArgumentCaptor<ListenableFuture> captorFuture   = ArgumentCaptor.forClass(ListenableFuture.class);
		final ArgumentCaptor<FutureCallback>   captorCallback = ArgumentCaptor.forClass(FutureCallback.class);
		
		verifyStatic();
		//noinspection unchecked
		Futures.addCallback(captorFuture.capture(), captorCallback.capture());
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