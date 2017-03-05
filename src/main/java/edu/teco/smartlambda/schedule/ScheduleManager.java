package edu.teco.smartlambda.schedule;

import edu.teco.smartlambda.Application;
import org.hibernate.Session;

import javax.persistence.LockModeType;
import java.util.Calendar;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 *
 */
public class ScheduleManager extends Thread {
	private static ScheduleManager instance;
	
	public static ScheduleManager getInstance() {
		if (instance == null) {
			instance = new ScheduleManager();
		}
		return instance;
	}
	
	private void ScheduleManager() {}
	
	public void run() {
		Event event;
		while (true) {
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.beginTransaction();
			Event    query         = from(Event.class);
			Calendar lockTolerance = Calendar.getInstance();
			lockTolerance.add(Calendar.MINUTE, -2);
			where(query.getNextExecution()).lte(Calendar.getInstance())
					.and(where(query.getLock()).isNull().or(query.getLock()).lte(lockTolerance));
			event = select(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1).get(session).get();
			if (event != null) {
				event.setLock(Calendar.getInstance());
				event.execute();
				session.update(event);
				session.getTransaction().commit();
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
	}
}
