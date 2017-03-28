package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.runtime.ExecutionResult;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.LockModeType;
import java.util.Calendar;
import java.util.HashMap;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * A class that manages the execution of scheduled events
 */
public class ScheduleManager {
	private static ScheduleManager instance;
	@Getter
	@Setter
	private boolean running = true;
	
	public static ScheduleManager getInstance() {
		if (instance == null) {
			instance = new ScheduleManager();
		}
		return instance;
	}
	
	private ScheduleManager() {}
	
	/**
	 * Waits until events are due, acquires them and repeatedly updates their lock state while executing them
	 *
	 * @return nothing
	 */
	public Void run() {
		final HashMap<Event, ListenableFuture<ExecutionResult>> futures = new HashMap<>(0);
		
		while (this.running) {
			final Event event;
			
			//Checks all started futures, if they're still running -> update lock, else -> update nextExecutionTime
			futures.forEach((Event e, ListenableFuture<ExecutionResult> future) -> {
				if (!future.isDone()) {
					Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
					e.setLock(Calendar.getInstance());
					Application.getInstance().getSessionFactory().getCurrentSession().update(e);
					Application.getInstance().getSessionFactory().getCurrentSession().getTransaction().commit();
				} else {
					Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
					e.setLock(null);
					e.save();
					Application.getInstance().getSessionFactory().getCurrentSession().getTransaction().commit();
				}
			});
			
			futures.keySet().removeIf(v -> futures.get(v).isDone());
			
			if (Application.getInstance().getSessionFactory().getCurrentSession().getTransaction() == null ||
					!Application.getInstance().getSessionFactory().getCurrentSession().getTransaction().isActive())
				Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
			
			final Event    query         = from(Event.class);
			final Calendar lockTolerance = Calendar.getInstance();
			lockTolerance.add(Calendar.MINUTE, -5);
			
			//Gets next scheduled event from the database
			where(query.getNextExecution()).lte(Calendar.getInstance()).and(query.getLock()).isNull().or(query.getLock())
					.lte(lockTolerance);
			event = select(query).setLockMode(LockModeType.PESSIMISTIC_WRITE).setMaxResults(1)
					.get(Application.getInstance().getSessionFactory().getCurrentSession()).orElse(null);
			
			if (event != null) {
				event.setLock(Calendar.getInstance());
				Application.getInstance().getSessionFactory().getCurrentSession().update(event);
				futures.put(event, event.execute());
				Application.getInstance().getSessionFactory().getCurrentSession().getTransaction().commit();
			} else {
				Application.getInstance().getSessionFactory().getCurrentSession().getTransaction().rollback();
			}
			try {
				Thread.sleep(1000);
			} catch (final InterruptedException ignored) {
			}
		}
		return null;
	}
}
