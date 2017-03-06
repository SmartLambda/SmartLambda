package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;

import javax.persistence.LockModeType;
import java.util.Calendar;
import java.util.HashMap;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * Created by Melanie on 01.03.2017.
 */
public class ScheduleManager {
	private static ScheduleManager instance;
	@Getter
	@Setter
	private boolean notEnd = true;
	
	public static ScheduleManager getInstance() {
		if (instance == null) {
			instance = new ScheduleManager();
		}
		return instance;
	}
	
	private ScheduleManager() {}
	
	public Void run() {
		
		final HashMap<Event, ListenableFuture<ExecutionReturnValue>> futures = new HashMap<>(0);
		
		while (notEnd) {
			final Event   event;
			final Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.beginTransaction();
			
			//Checks all started futures, if they're still running -> update lock, else -> update nextExecutionTime
			futures.forEach((Event e, ListenableFuture<ExecutionReturnValue> future) -> {
				if (!future.isDone()) {
					e.setLock(Calendar.getInstance());
					session.update(e);
				} else {
					e.save();
					futures.remove(e, future);
				}
			});
			
			final Event    query         = from(Event.class);
			final Calendar lockTolerance = Calendar.getInstance();
			lockTolerance.add(Calendar.MINUTE, -5);
			
			//Gets next scheduled event from the database
			where(query.getNextExecution()).lte(Calendar.getInstance()).and(query.getLock()).isNull().or(query.getLock())
					.lte(lockTolerance);
			event = select(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1).get(session).orElse(null);
			
			if (event != null) {
				event.setLock(Calendar.getInstance());
				session.update(event);
				session.getTransaction().commit();
				futures.put(event, event.execute());
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
		return null;
	}
}
