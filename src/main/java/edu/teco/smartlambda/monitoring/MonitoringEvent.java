package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Calendar;

@Entity
@Table(appliesTo = "MonitoringEvent")
@Data
public class MonitoringEvent {
	
	// TODO are there more immutable attributes?
	private       Calendar            time;
	private final User                lambdaOwner;
	private final String              lambdaName;
	private       long                duration;
	private       int                 CPUTime;
	private       String              error;
	private final MonitoringEventType type;
	private final Key                 key;
	private int                       id;
	
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	public int getId() {
		return id;
	}
	
	@Temporal(TemporalType.DATE)
	@Column(name = "time")
	public Calendar getTime() {
		return time;
	}
	
	@Column(name = "lambdaOwner")
	public User getLambdaOwner() {
		return lambdaOwner;
	}
	
	@Column(name = "lambdaName")
	public String getLambdaName() {
		return lambdaName;
	}
	
	@Column(name = "duration")
	public long getDuration() {
		return duration;
	}
	
	@Column(name = "CPUTime")
	public int getCPUTime() {
		return CPUTime;
	}
	
	@Column(name = "error")
	public String getError() {
		return error;
	}
	
	@Column(name = "type")
	public MonitoringEventType getType() {
		return type;
	}
	
	@Column(name = "key")
	public Key getKey() {
		return key;
	}
	
	enum MonitoringEventType {
		EXECUTION, DELETION, DEPLOYMENT;
	}
}


