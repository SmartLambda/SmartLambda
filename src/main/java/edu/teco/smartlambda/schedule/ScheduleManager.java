package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;

import javax.persistence.LockModeType;
import java.util.AbstractMap;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 *
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
	
	private void ScheduleManager() {}
	
	public Void run() {
		Event                                                                        event;
		List<AbstractMap.SimpleEntry<Event, ListenableFuture<ExecutionReturnValue>>> futures = new LinkedList<>();
		
		while (notEnd) {
			Session session = Application.getInstance().getSessionFactory().getCurrentSession();
			session.beginTransaction();
			
			//Checks all started futures, if they're still running -> update lock, else -> update nextExecutionTime
			for (AbstractMap.SimpleEntry<Event, ListenableFuture<ExecutionReturnValue>> future : futures) {
				if (!future.getValue().isDone()) {
					future.getKey().setLock(Calendar.getInstance());
					session.update(future.getKey());
				} else {
					future.getKey().save();
					futures.remove(future);
				}
			}
			
			Event    query         = from(Event.class);
			Calendar lockTolerance = Calendar.getInstance();
			lockTolerance.add(Calendar.MINUTE, -2);
			
			//Gets next scheduled event from the database
			where(query.getNextExecution()).lte(Calendar.getInstance()).and(query.getLock()).isNull().or(query.getLock())
					.lte(lockTolerance);
			event = select(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1).get(session).orElse(null);
			
			if (event != null) {
				event.setLock(Calendar.getInstance());
				session.update(event);
				session.getTransaction().commit();
				futures.add(new AbstractMap.SimpleEntry<>(event, event.execute()));
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				
			}
		}
		return null;
	}
}
