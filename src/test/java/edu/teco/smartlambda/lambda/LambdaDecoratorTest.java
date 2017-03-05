package edu.teco.smartlambda.lambda;

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

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
	}
}