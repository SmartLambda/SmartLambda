package edu.teco.smartlambda.schedule;

import edu.teco.smartlambda.Application;

import java.util.Calendar;
import java.util.List;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.orderBy;
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
		List<Event> events;
		while (true) {
			Event query = from(Event.class);
			where(query.getNextExecution()).lte(Calendar.getInstance());
			orderBy(query.getNextExecution());
			events = select(query).list(Application.getInstance().getSessionFactory().getCurrentSession());
			for (Event event : events) {
				event.execute();
			}
			try {
				sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
	}
}
