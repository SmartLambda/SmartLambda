package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.concurrent.ThreadManager;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Calendar;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 *
 */
public class MonitoringServiceTest {
	
	private AbstractLambda lambda;
	private final MonitoringService monitoringService = MonitoringService.getInstance();
	private MonitoringEvent actualEvent;
	
	@Before
	public void setUp() {
		this.lambda = Mockito.mock(Lambda.class);
		Mockito.when(this.lambda.getName()).thenReturn("test");
		Mockito.when(this.lambda.getOwner()).thenReturn(Mockito.mock(User.class));
	}
	
	@Test
	public void onLambdaExecutionStartTest() {
		AuthenticationService.getInstance().authenticate(Mockito.mock(Key.class));
		this.actualEvent = this.monitoringService.onLambdaExecutionStart(this.lambda);
		
		final MonitoringEvent expectedEvent = new MonitoringEvent(this.lambda, MonitoringEvent.MonitoringEventType.EXECUTION,
				AuthenticationService.getInstance().getAuthenticatedKey().get());
		expectedEvent.setTime(this.actualEvent.getTime());
		
		Assert.assertTrue((expectedEvent.getTime().equals(this.actualEvent.getTime())) &&
				(expectedEvent.getLambdaName().equals(this.actualEvent.getLambdaName())) &&
				(expectedEvent.getLambdaOwner().equals(this.actualEvent.getLambdaOwner())) &&
				(expectedEvent.getKey().equals(this.actualEvent.getKey())) && (expectedEvent.getType().equals(this.actualEvent.getType()
		)));
	}
	
	@Test
	public void onLambdaExecutionEndErrorTest() {
		AuthenticationService.getInstance().authenticate(Mockito.mock(Key.class));
		final Exception e = new NullPointerException("");
		this.actualEvent = this.monitoringService.onLambdaExecutionStart(this.lambda);
		this.monitoringService.onLambdaExecutionEnd(this.lambda, 0, new ExecutionReturnValue(null, e), this.actualEvent);
		
		final MonitoringEvent expectedEvent = new MonitoringEvent(this.lambda, MonitoringEvent.MonitoringEventType.EXECUTION,
				AuthenticationService.getInstance().getAuthenticatedKey().orElse(null));
		expectedEvent.setTime(this.actualEvent.getTime());
		expectedEvent.setError(e.getStackTrace().toString());
		expectedEvent.setDuration(Calendar.getInstance().getTimeInMillis() - expectedEvent.getTime().getTimeInMillis());
		expectedEvent.setCPUTime(0);
		
		final MonitoringEvent query = from(MonitoringEvent.class);
		where(query.getLambdaName()).eq(this.actualEvent.getLambdaName()).and(query.getLambdaOwner()).eq(this.actualEvent.getLambdaOwner
				());
		this.actualEvent = select(query).get(Application.getInstance().getSessionFactory().getCurrentSession()).get();
		
		Assert.assertTrue((expectedEvent.getTime().equals(this.actualEvent.getTime())) &&
				(expectedEvent.getLambdaName().equals(this.actualEvent.getLambdaName())) &&
				(expectedEvent.getLambdaOwner().equals(this.actualEvent.getLambdaOwner())) &&
				(expectedEvent.getKey().equals(this.actualEvent.getKey())) && expectedEvent.getType().equals(this.actualEvent.getType()) &&
				expectedEvent.getError().equals(this.actualEvent.getError()) &&
				expectedEvent.getDuration() == this.actualEvent.getDuration());
	}
	
	@Test
	public void onLambdaDeletionTest() {
		AuthenticationService.getInstance().authenticate(Mockito.mock(Key.class));
		this.monitoringService.onLambdaDeletion(this.lambda);
		
		final MonitoringEvent expectedEvent = new MonitoringEvent(this.lambda, MonitoringEvent.MonitoringEventType.DELETION,
				AuthenticationService.getInstance().getAuthenticatedKey().get());
		
		final MonitoringEvent query = from(MonitoringEvent.class);
		where(query.getLambdaName()).eq(expectedEvent.getLambdaName()).and(query.getLambdaOwner()).eq(expectedEvent.getLambdaOwner());
		this.actualEvent = select(query).get(Application.getInstance().getSessionFactory().getCurrentSession()).get();
		expectedEvent.setTime(this.actualEvent.getTime());
		
		Assert.assertTrue((expectedEvent.getTime().equals(this.actualEvent.getTime())) &&
				(expectedEvent.getLambdaName().equals(this.actualEvent.getLambdaName())) &&
				(expectedEvent.getLambdaOwner().equals(this.actualEvent.getLambdaOwner())) &&
				(expectedEvent.getKey().equals(this.actualEvent.getKey())) && (expectedEvent.getType().equals(this.actualEvent.getType()
		)));
	}
	
	@Test
	public void onLambdaDeploymentTest() {
		AuthenticationService.getInstance().authenticate(Mockito.mock(Key.class));
		this.monitoringService.onLambdaDeletion(this.lambda);
		final MonitoringEvent expectedEvent = new MonitoringEvent(this.lambda, MonitoringEvent.MonitoringEventType.DEPLOYMENT,
				AuthenticationService.getInstance().getAuthenticatedKey().get());
		
		final MonitoringEvent query = from(MonitoringEvent.class);
		where(query.getLambdaName()).eq(expectedEvent.getLambdaName()).and(query.getLambdaOwner()).eq(expectedEvent.getLambdaOwner());
		this.actualEvent = select(query).get(Application.getInstance().getSessionFactory().getCurrentSession()).get();
		expectedEvent.setTime(this.actualEvent.getTime());
		
		Assert.assertTrue((expectedEvent.getTime().equals(this.actualEvent.getTime())) &&
				(expectedEvent.getLambdaName().equals(this.actualEvent.getLambdaName())) &&
				(expectedEvent.getLambdaOwner().equals(this.actualEvent.getLambdaOwner())) &&
				(expectedEvent.getKey().equals(this.actualEvent.getKey())) && (expectedEvent.getType().equals(this.actualEvent.getType()
		)));
	}
	
	@Test(expected = NotAuthenticatedException.class)
	public void onLambdaExecutionStartNotAuthenticatedExceptionTest() {
		ThreadManager.getExecutorService().submit(AuthenticationService::getInstance);
		this.monitoringService.onLambdaExecutionStart(this.lambda);
	}
	
	@After
	public void tearDown() {
		Application.getInstance().getSessionFactory().getCurrentSession().delete(this.actualEvent);
	}
}