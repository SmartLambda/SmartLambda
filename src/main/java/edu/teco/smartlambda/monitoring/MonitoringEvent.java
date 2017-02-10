package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.authentication.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;

/**
 * Created by Melanie on 29.01.2017.
 */
public class MonitoringEvent {
	@Getter
	@Setter
	private Calendar            time;
	@Getter
	@Setter
	private User                lambdaOwner;
	@Getter
	@Setter
	private String              lambdaName;
	@Getter
	@Setter
	private long                duration;
	@Getter
	@Setter
	private int                 CPUTime;
	@Getter
	@Setter
	private String              error;
	@Getter
	@Setter
	private MonitoringEventType status;
	
	enum MonitoringEventType {
		EXECUTION, DELETION, DEPLOYMENT;
	}
}


