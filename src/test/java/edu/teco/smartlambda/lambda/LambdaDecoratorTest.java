package edu.teco.smartlambda.lambda;

import org.junit.Test;

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