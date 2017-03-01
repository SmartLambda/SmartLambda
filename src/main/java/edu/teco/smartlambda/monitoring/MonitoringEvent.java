package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import lombok.Data;
import org.hibernate.Session;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Calendar;

@Entity
@Table(name = "MonitoringEvent")
@Data
public class MonitoringEvent {
	
	@Temporal(TemporalType.DATE)
	private final Calendar            time;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lambdaOwner")
	private final User                lambdaOwner;
	private final String              lambdaName;
	private       long                duration;
	private       int                 CPUTime;
	private       String              error;
	private final MonitoringEventType type;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "key")
	private final Key                 key;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int                       id;
	
	private Session session = Application.getInstance().getSessionFactory().getCurrentSession();
	
	
	public void save() {
		session.beginTransaction();
		session.save(this);
		session.getTransaction().commit();
	}
	
	enum MonitoringEventType {
		EXECUTION, DELETION, DEPLOYMENT;
	}
}


