package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.authentication.entities.User;
import lombok.Data;

import java.util.Calendar;

@Data
public class MonitoringEvent {
	
	// TODO are there more immutable attributes?
	private       Calendar            time;
	private final User                lambdaOwner;
	private final String              lambdaName;
	private       long                duration;
	private       int                 CPUTime;
	private       String              error;
	private final MonitoringEventType status;
	
	enum MonitoringEventType {
		EXECUTION, DELETION, DEPLOYMENT;
	}
}


