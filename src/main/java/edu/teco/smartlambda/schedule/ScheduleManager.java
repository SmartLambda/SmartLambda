package edu.teco.smartlambda.schedule;

import edu.teco.smartlambda.Application;

import javax.persistence.LockModeType;
import java.util.Calendar;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * Created by Melanie on 01.03.2017.
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
			Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
			Event query = from(Event.class);
			where(query.getNextExecution()).lte(Calendar.getInstance()).and(query.getLock()).isNull();
			event = select(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1)
					.get(Application.getInstance().getSessionFactory().getCurrentSession()).get();
			if (event != null) event.setLock(Calendar.getInstance());
				event.execute();
			event.save();
			Application.getInstance().getSessionFactory().getCurrentSession().getTransaction().commit();
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
	}
}
