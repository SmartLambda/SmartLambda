package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import lombok.Data;
import org.hibernate.Session;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Calendar;

@Entity
@Table(appliesTo = "MonitoringEvent")
@Data
public class MonitoringEvent {
	
	@Temporal(TemporalType.DATE)
	@Column(name = "time")
	private final Calendar            time;
	@Column(name = "lambdaOwner")
	private final User                lambdaOwner;
	@Column(name = "lambdaName")
	private final String              lambdaName;
	@Column(name = "duration")
	private       long                duration;
	@Column(name = "CPUTime")
	private       int                 CPUTime;
	@Column(name = "error")
	private       String              error;
	@Column(name = "type")
	private final MonitoringEventType type;
	@Column(name = "key")
	private final Key                 key;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int                       id;
	
	private Session session = Application.getInstance().getSessionFactory().openSession();
	
	
	public void save() {
		session.beginTransaction();
		session.save(this);
		session.getTransaction().commit();
	}
	
	enum MonitoringEventType {
		EXECUTION, DELETION, DEPLOYMENT;
	}
}


