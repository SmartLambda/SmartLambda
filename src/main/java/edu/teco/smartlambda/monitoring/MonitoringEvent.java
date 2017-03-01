package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.AbstractLambda;
import lombok.Getter;
import lombok.Setter;
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
public class MonitoringEvent {
	
	@Temporal(TemporalType.DATE)
	@Getter
	private Calendar            time;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lambdaOwner")
	@Getter
	private  User                lambdaOwner;
	@Getter
	private  String              lambdaName;
	@Setter
	private       long                duration;
	@Setter
	private       int                 CPUTime;
	@Setter
	private       String              error;
	@Getter
	private MonitoringEventType type;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "key")
	@Getter
	private Key                 key;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int                       id;
	
	public MonitoringEvent() {
		
	}
	
	public MonitoringEvent(final AbstractLambda lambda, final MonitoringEventType type, final Key key) {
		
		this.time =Calendar.getInstance();
		this.lambdaOwner= lambda.getOwner();
		this.lambdaName=lambda.getName();
		this.type = type;
		this.key=key;
	}
	
		
	public void save() {
		Session session = Application.getInstance().getSessionFactory().getCurrentSession();
		session.save(this);
		session.getTransaction().commit();
	}
	
	enum MonitoringEventType {
		EXECUTION, DELETION, DEPLOYMENT;
	}
}


