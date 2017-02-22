package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.container.ExecutionReturnValue;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
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
	public Optional<ExecutionReturnValue> execute(final String params, final boolean async) {
		return this.lambda.execute(params, async);
	}
	
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
}
