package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.lambda.Lambda;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Calendar;

import static org.mockito.Mockito.*;

/**
 * Created by Melanie on 29.01.2017.
 */
public class MonitoringServiceTest {
	
	MonitoringService monitoringService = new MonitoringService();
	MonitoringEvent event;
	AbstractLambda mockLambda = mock(Lambda.class);
	
	@BeforeClass
	public void buildUp() {
		event = new MonitoringEvent();
		event.setLambdaName(mockLambda.getName());
		event.setLambdaOwner(mockLambda.getOwner());
	}
	
	@Before
	public void setUp() {
		event.setTime(Calendar.getInstance());
	}
	
	@Test
	public void onLambdaExecutionStartTest() {
		event.setStatus(MonitoringEvent.MonitoringEventType.EXECUTION);
		monitoringService.onLambdaExecutionStart(mockLambda);
		Assert.assertTrue(areEqual(event, monitoringService.getMonitoringEvent()));
	}
	
	@Test
	public void onLambdaExecutionEndTest() {
		event.setStatus(MonitoringEvent.MonitoringEventType.EXECUTION);
		monitoringService.onLambdaExecutionStart(mockLambda);
		event.setCPUTime(10);
		event.setDuration(Calendar.getInstance().getTimeInMillis()-event.getTime().getTimeInMillis());
		monitoringService.onLambdaExecutionEnd(mockLambda, 10);
		Assert.assertTrue(areEqual(event, monitoringService.getMonitoringEvent()));
	}
	
	@Test
	public void onLambdaExecutionEndErrorTest() {
		event.setStatus(MonitoringEvent.MonitoringEventType.EXECUTION);
		monitoringService.onLambdaExecutionStart(mockLambda);
		event.setCPUTime(10);
		event.setDuration(Calendar.getInstance().getTimeInMillis()-event.getTime().getTimeInMillis());
		event.setError("Error");
		monitoringService.onLambdaExecutionEnd(mockLambda,10, "Error");
		Assert.assertTrue(areEqual(event, monitoringService.getMonitoringEvent()));
	}
	
	@Test
	public void onLambdaDeletionTest() {
		event.setStatus(MonitoringEvent.MonitoringEventType.DELETION);
		monitoringService.onLambdaDeletion(mockLambda);
		Assert.assertTrue(areEqual(event, monitoringService.getMonitoringEvent()));
	}
	
	@Test
	public void onLambdaDeploymentTest() {
		event.setStatus(MonitoringEvent.MonitoringEventType.DEPLOYMENT);
		monitoringService.onLambdaDeployment(mockLambda);
		Assert.assertTrue(areEqual(event, monitoringService.getMonitoringEvent()));
	}
	
	@After
	public void tearDown() {
		event = null;
	}
	
	private boolean areEqual(MonitoringEvent expected, MonitoringEvent actual) {
		Calendar diffTime = expected.getTime();
		diffTime.add(Calendar.SECOND, 1);
		return ((expected.getLambdaName().equals(actual.getLambdaName()) && expected.getLambdaOwner().equals(actual.getLambdaOwner()) &&
		        expected.getCPUTime() == actual.getCPUTime() && expected.getDuration() == actual.getDuration() && expected.getError().equals(actual
				                                                                                                                           .getError()) && expected.getStatus().equals(actual.getStatus())) && ((expected.getTime() == actual.getTime())|| diffTime == actual.getTime())) ? true : false;
	}
	
}
