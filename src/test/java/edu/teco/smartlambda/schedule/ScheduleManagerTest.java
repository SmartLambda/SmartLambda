package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.concurrent.ThreadManager;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.torpedoquery.jpa.Torpedo;

import java.util.LinkedList;
import java.util.List;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;

/**
 * Created by Melanie on 05.03.2017.
 */
public class ScheduleManagerTest {
	
	ListenableFuture<Void> future;
	List<Event> events = new LinkedList<>();
	Event someEvent;
	
	@Before
	public void setUp() {
		future = ThreadManager.getExecutorService().submit(ScheduleManager.getInstance()::run);
		for (int i = 0; i < 5; i++) {
			events.add(Mockito.mock(Event.class));
		}
		for (Event e : events) {
			e.save();
		}
		someEvent = Mockito.mock(Event.class);
	}
	
	@Test
	@Ignore
	public void runTest() {
		MonitoringEvent query = from(MonitoringEvent.class);
		Torpedo.where(query.getLambdaName()).eq(events.get(1).getLambda().getName()).and(query.getLambdaOwner())
				.eq(events.get(1).getLambda().getOwner());
		MonitoringEvent result = select(query).setMaxResults(1).get(Application.getInstance().getSessionFactory().getCurrentSession()).orElse(null);
		Assert.assertNotNull(result);
	}
	
	@Test
	public void saveTest() {
		Event query = from(Event.class);
		Torpedo.where(query.getName()).eq(someEvent.getName());
		Event result = select(query).setMaxResults(1).get(Application.getInstance().getSessionFactory().getCurrentSession()).orElse(null);
		Assert.assertEquals(someEvent, result);
	}
	
	@After
	public void tearDown() {
		future.cancel(true);
	}
}