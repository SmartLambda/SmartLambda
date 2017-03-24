package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.runtime.ExecutionResult;
import edu.teco.smartlambda.runtime.Runtime;
import edu.teco.smartlambda.schedule.Event;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class LambdaDecoratorTest {
	@Test
	public void unwrap() throws Exception {
		final Lambda lambda = mock(Lambda.class);
		assertSame(lambda, LambdaDecorator.unwrap(lambda));
		
		final LambdaDecorator decorated = new LambdaDecorator(lambda) {
		};
		assertSame(lambda, LambdaDecorator.unwrap(decorated));
		
		final LambdaDecorator decoratedTwice = new LambdaDecorator(decorated) {
		};
		assertSame(lambda, LambdaDecorator.unwrap(decoratedTwice));
		
		assertNull(LambdaDecorator.unwrap(null));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void unwrapFail() throws Exception {
		LambdaDecorator.unwrap(new AbstractLambda() {
			@Override
			public ExecutionResult executeSync(final String params) {
				return null;
			}
			
			@Override
			public ListenableFuture<ExecutionResult> executeAsync(final String params) {
				return null;
			}
			
			@Override
			public void save() {
				
			}
			
			@Override
			public void update() {
				
			}
			
			@Override
			public void delete() {
				
			}
			
			@Override
			public void schedule(final Event event) {
				
			}
			
			@Override
			public void deployBinary(final byte[] content) {
				
			}
			
			@Override
			public Optional<Event> getScheduledEvent(final String name) {
				return null;
			}
			
			@Override
			public List<Event> getScheduledEvents() {
				return null;
			}
			
			@Override
			public List<MonitoringEvent> getMonitoringEvents() {
				return null;
			}
			
			@Override
			public String getName() {
				return null;
			}
			
			@Override
			public User getOwner() {
				return null;
			}
			
			@Override
			public boolean isAsync() {
				return false;
			}
			
			@Override
			public Runtime getRuntime() {
				return null;
			}
			
			@Override
			public void setName(final String name) {
				
			}
			
			@Override
			public void setOwner(final User owner) {
				
			}
			
			@Override
			public void setAsync(final boolean async) {
				
			}
			
			@Override
			public void setRuntime(final Runtime runtime) {
				
			}
		});
	}
	
	@Test
	public void testDecoratorMethods() throws Exception {
		final Lambda mockedLambda = mock(Lambda.class);
		final LambdaDecorator decorator = new LambdaDecorator(mockedLambda) {
		};
		
		decorator.executeAsync("");
		verify(mockedLambda).executeAsync("");
		
		decorator.executeSync("");
		verify(mockedLambda).executeSync("");
		
		decorator.getMonitoringEvents();
		verify(mockedLambda).getMonitoringEvents();
		
		decorator.delete();
		verify(mockedLambda).delete();
		
		decorator.deployBinary(null);
		verify(mockedLambda).deployBinary(null);
		
		decorator.getName();
		verify(mockedLambda).getName();
		
		decorator.getOwner();
		verify(mockedLambda).getOwner();
		
		decorator.getRuntime();
		verify(mockedLambda).getRuntime();
		
		decorator.getScheduledEvent("");
		verify(mockedLambda).getScheduledEvent("");
		
		decorator.getScheduledEvents();
		verify(mockedLambda).getScheduledEvents();
		
		decorator.isAsync();
		verify(mockedLambda).isAsync();
		
		decorator.save();
		verify(mockedLambda).save();
		
		decorator.schedule(null);
		verify(mockedLambda).schedule(null);
		
		decorator.update();
		verify(mockedLambda).update();
		
		decorator.setOwner(null);
		verify(mockedLambda).setOwner(null);
		
		decorator.setName("");
		verify(mockedLambda).setName("");
		
		decorator.setRuntime(null);
		verify(mockedLambda).setRuntime(null);
		
		decorator.setAsync(true);
		verify(mockedLambda).setAsync(true);
	}
}