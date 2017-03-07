package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.runtime.ExecutionResult;
import edu.teco.smartlambda.runtime.Runtime;
import edu.teco.smartlambda.schedule.Event;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

/**
 * A decorator for lambdas that can be extended and partially overridden to add behaviour to existing lambda calls. By calling the super
 * method, the lambda functionality or any decorator surrounding the lambda will be called.
 */
@RequiredArgsConstructor
public abstract class LambdaDecorator extends AbstractLambda {
	protected final AbstractLambda lambda;
	
	@Override
	public Optional<ExecutionResult> executeSync(final String params) {
		return this.lambda.executeSync(params);
	}
	
	@Override
	public ListenableFuture<ExecutionResult> executeAsync(final String params) {return this.lambda.executeAsync(params);}
	
	@Override
	public void save() {
		this.lambda.save();
	}
	
	@Override
	public void update() {
		this.lambda.update();
	}
	
	@Override
	public void delete() {
		this.lambda.delete();
	}
	
	@Override
	public void schedule(final Event event) {
		this.lambda.schedule(event);
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		this.lambda.deployBinary(content);
	}
	
	@Override
	public Event getScheduledEvent(final String name) {
		return this.lambda.getScheduledEvent(name);
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		return this.lambda.getScheduledEvents();
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		return this.lambda.getMonitoringEvents();
	}
	
	@Override
	public final String getName() {
		return this.lambda.getName();
	}
	
	@Override
	public final User getOwner() {
		return this.lambda.getOwner();
	}
	
	@Override
	public final boolean isAsync() {
		return this.lambda.isAsync();
	}
	
	@Override
	public final Runtime getRuntime() {
		return this.lambda.getRuntime();
	}
	
	@Override
	public final void setName(final String name) {
		this.lambda.setName(name);
	}
	
	@Override
	public final void setOwner(final User owner) {
		this.lambda.setOwner(owner);
	}
	
	@Override
	public final void setAsync(final boolean async) {
		this.lambda.setAsync(async);
	}
	
	@Override
	public void setRuntime(final Runtime runtime) {
		this.lambda.setRuntime(runtime);
	}
	
	public static Lambda unwrap(final AbstractLambda abstractLambda) {
		if (abstractLambda == null) return null;
		
		if (abstractLambda instanceof Lambda) return (Lambda) abstractLambda;
		
		if (abstractLambda instanceof LambdaDecorator) return unwrap(((LambdaDecorator) abstractLambda).lambda);
		
		throw new IllegalArgumentException("Can't unwrap object of type " + abstractLambda.getClass().getName());
	}
}
