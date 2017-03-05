package edu.teco.smartlambda.schedule;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.concurrent.ThreadManager;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.torpedoquery.jpa.Torpedo;

import java.util.List;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;

/**
 * Created by Melanie on 05.03.2017.
 */
public class ScheduleManagerTest {
	
	ListenableFuture<Void> future;
	List<Event>            events;
	
	@Before
	public void setUp() {
		future = ThreadManager.getExecutorService().submit(ScheduleManager.getInstance()::run);
		for (int i = 0; i < 5; i++) {
			events.add(Mockito.mock(Event.class));
		}
		for (Event e : events) {
			e.save();
		}
	}
	
	public void runTest() {
		ScheduleManager.getInstance().run();
		MonitoringEvent query = from(MonitoringEvent.class);
		Torpedo.where(query.getLambdaName()).eq(events.get(0).getLambda().getName()).and(query.getLambdaOwner())
				.eq(events.get(0).getLambda().getOwner());
		MonitoringEvent result =
				select(query).setMaxResults(1).get(Application.getInstance().getSessionFactory().getCurrentSession()).orElse(null);
		Assert.assertNotNull(result);
	}
	
	@After
	public void tearDown() {
		future.cancel(true);
	}
}