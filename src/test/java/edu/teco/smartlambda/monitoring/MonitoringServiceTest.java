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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Application.class, AuthenticationService.class})
public class MonitoringServiceTest {
	
	private AbstractLambda lambda;
	private final MonitoringService monitoringService = MonitoringService.getInstance();
	private        MonitoringEvent actualEvent;
	private        MonitoringEvent saveEvent;
	private        User            user;
	private static SessionFactory  sessionFactory;
	
	/*@BeforeClass
	public static void initialSetUp() {
		sessionFactory.getCurrentSession().beginTransaction();
	}*/
	
	@Before
	public void setUp() {
		sessionFactory = Mockito.mock(SessionFactory.class);
		Session     session = Mockito.mock(Session.class);
		Application app     = Mockito.mock(Application.class);
		PowerMockito.mockStatic(Application.class);
		PowerMockito.when(Application.getInstance()).thenReturn(app);
		PowerMockito.when(Application.getInstance().getSessionFactory()).thenReturn(sessionFactory);
		PowerMockito.when(Application.getInstance().getSessionFactory().getCurrentSession()).thenReturn(session);
		
		PowerMockito.when(session.save(any(MonitoringEvent.class))).thenAnswer(invocation -> {
			saveEvent = (MonitoringEvent) invocation.getArguments()[0];
			return null;
		});
		this.lambda = Mockito.mock(Lambda.class);
		Mockito.when(this.lambda.getName()).thenReturn("test");
		Mockito.when(this.lambda.getOwner()).thenReturn(Mockito.mock(User.class));
	}
	
	private void mockAuthentication() {
		PowerMockito.mockStatic(AuthenticationService.class);
		AuthenticationService authService = Mockito.mock(AuthenticationService.class);
		PowerMockito.when(AuthenticationService.getInstance()).thenReturn(authService);
		PowerMockito.when(authService.getAuthenticatedKey()).thenReturn(Optional.of(Mockito.mock(Key.class)));
	}
	
	@Test
	public void onLambdaExecutionStartTest() {
		this.mockAuthentication();
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
		this.mockAuthentication();
		final Exception e = new NullPointerException("");
		this.actualEvent = this.monitoringService.onLambdaExecutionStart(this.lambda);
		this.monitoringService.onLambdaExecutionEnd(this.lambda, 0, new ExecutionReturnValue(null, e), this.actualEvent);
		
		final ExecutionReturnValue exRetVal = new ExecutionReturnValue(null, e);
		final MonitoringEvent expectedEvent = new MonitoringEvent(this.lambda, MonitoringEvent.MonitoringEventType.EXECUTION,
				AuthenticationService.getInstance().getAuthenticatedKey().orElse(null));
		expectedEvent.setTime(this.actualEvent.getTime());
		expectedEvent.setError(exRetVal.getException().get());
		expectedEvent.setDuration(this.actualEvent.getDuration());
		expectedEvent.setCPUTime(0);
		
		Assert.assertTrue((expectedEvent.getTime().equals(this.actualEvent.getTime())) &&
				(expectedEvent.getLambdaName().equals(this.actualEvent.getLambdaName())) &&
				(expectedEvent.getLambdaOwner().equals(this.actualEvent.getLambdaOwner())) &&
				(expectedEvent.getKey().equals(this.actualEvent.getKey())) && expectedEvent.getType().equals(this.actualEvent.getType()) &&
				expectedEvent.getError().equals(this.actualEvent.getError()) &&
				expectedEvent.getDuration() == this.actualEvent.getDuration());
	}
	
	@Test
	public void onLambdaDeletionTest() {
		this.mockAuthentication();
		this.monitoringService.onLambdaDeletion(this.lambda);
		
		final MonitoringEvent expectedEvent = new MonitoringEvent(this.lambda, MonitoringEvent.MonitoringEventType.DELETION,
				AuthenticationService.getInstance().getAuthenticatedKey().get());
		
		expectedEvent.setTime(this.actualEvent.getTime());
		
		Assert.assertTrue((expectedEvent.getTime().equals(this.actualEvent.getTime())) &&
				(expectedEvent.getLambdaName().equals(this.actualEvent.getLambdaName())) &&
				(expectedEvent.getLambdaOwner().equals(this.actualEvent.getLambdaOwner())) &&
				(expectedEvent.getKey().equals(this.actualEvent.getKey())) && (expectedEvent.getType().equals(this.actualEvent.getType()
		)));
	}
	
	@Test
	public void onLambdaDeploymentTest() {
		this.mockAuthentication();
		this.monitoringService.onLambdaDeployment(this.lambda);
		final MonitoringEvent expectedEvent = new MonitoringEvent(this.lambda, MonitoringEvent.MonitoringEventType.DEPLOYMENT,
				AuthenticationService.getInstance().getAuthenticatedKey().get());
		
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
		/*Session session = sessionFactory.getCurrentSession();
		if(actualEvent != null) {
		session.delete(this.actualEvent); } */
		
	}
}