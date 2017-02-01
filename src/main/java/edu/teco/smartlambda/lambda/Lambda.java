package edu.teco.smartlambda.lambda;

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

/**
 * Created by Melanie on 01.02.2017.
 */
public class Lambda extends AbstractLambda {
	@Getter@Setter
	private User    owner;
	@Getter@Setter
	private String  name;
	@Getter@Setter
	private boolean async;
	@Getter
	private String  runtime;
	private String  containerId;
	
	public Optional<String> execute(String params, boolean async) {
		return null;
	}
	public void save() {}
	public void delete() {}
	public void update() {}
	public void schedule(Event event) {}
	public void deployBinary(byte[] content) {}
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
