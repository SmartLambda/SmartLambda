package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.schedule.Event;

import java.util.List;
import java.util.Optional;

/**
 * Created by Melanie on 01.02.2017.
 */
public class PermissionDecorator extends LambdaDecorator {
	public Optional<String> execute(String params, boolean async) {
		return null;
	}
	public void save() {}
	public void update() {}
	public void delete() {}
	public void schedule(Event event) {}
	public void deployBinary(byte[] conten) {}
	public Event getScheduledEvent() {
		return null;
	}
	public List<Event> getScheduledEvents() {
		return null;
	}
	public List<MonitoringEvent> getMonitoringEvents() {
		return null;
	}
	
}
