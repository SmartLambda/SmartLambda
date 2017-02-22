package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.schedule.Event;

import java.util.List;
import java.util.Optional;

/**
 * Decorates lambdas with calls to the monitoring service
 */
//// FIXME: 2/15/17 
public class MonitoringDecorator extends LambdaDecorator {
	
	public MonitoringDecorator(final AbstractLambda lambda) {
		super(lambda);
	}
	
	@Override
	public Optional<String> execute(final String params, final boolean async) {
		return super.execute(params, async);
	}
	
	@Override
	public void save() {
		super.save();
	}
	
	@Override
	public void update() {
		super.update();
	}
	
	@Override
	public void delete() {
		super.delete();
	}
	
	@Override
	public void schedule(final Event event) {
		super.schedule(event);
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		super.deployBinary(content);
	}
	
	@Override
	public Event getScheduledEvent(final String name) {
		return super.getScheduledEvent(name);
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		return super.getScheduledEvents();
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		return super.getMonitoringEvents();
	}
}
