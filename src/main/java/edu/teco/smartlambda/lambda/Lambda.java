package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.schedule.Event;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Optional;

public class Lambda extends AbstractLambda {
	
	@Getter
	@Setter
	private User owner;
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private boolean async;
	
	@Override
	public Optional<String> execute(final String params, final boolean async) {
		//// FIXME: 2/15/17 
		return null;
	}
	
	@Override
	public void save() {
		//// FIXME: 2/15/17 
	}
	
	@Override
	public void update() {
		//// FIXME: 2/15/17 
	}
	
	@Override
	public void delete() {
		//// FIXME: 2/15/17 
	}
	
	@Override
	public void schedule(final Event event) {
		//// FIXME: 2/15/17 
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		//// FIXME: 2/15/17 
	}
	
	@Override
	public Event getScheduledEvent(final String name) {
		//// FIXME: 2/15/17 
		return null;
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		//// FIXME: 2/15/17 
		return null;
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		//// FIXME: 2/15/17 
		return null;
	}
	
	@Override
	public Runtime getRuntime() {
		//// FIXME: 2/15/17
		return null;
	}
}
