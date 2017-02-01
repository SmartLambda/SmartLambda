package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.lambda.AbstractLambda;
import lombok.Getter;

import java.util.Calendar;

/**
 * Created by Melanie on 29.01.2017.
 */
public class MonitoringService {
	
	private ThreadLocal<MonitoringService> instance;
	@Getter private MonitoringEvent                monitoringEvent;
	
	public MonitoringService () {}
	
	private void monitoringService() {}
	
	public void onLambdaExecutionStart(AbstractLambda lambda) {
		monitoringEvent.setTime(Calendar.getInstance());
		monitoringEvent.setLambdaName(lambda.getName());
		monitoringEvent.setLambdaOwner(lambda.getOwner());
		monitoringEvent.setStatus(MonitoringEvent.MonitoringEventType.EXECUTION);
	}
	
	public void onLambdaExecutionEnd(AbstractLambda lambda, int CPUTime) {
		monitoringEvent.setCPUTime(CPUTime);
		monitoringEvent.setDuration(Calendar.getInstance().getTimeInMillis()-monitoringEvent.getTime().getTimeInMillis());
	}
	
	public void onLambdaExecutionEnd(AbstractLambda lambda, int CPUTime, String error) {
		monitoringEvent.setCPUTime(CPUTime);
		monitoringEvent.setDuration(Calendar.getInstance().getTimeInMillis()-monitoringEvent.getTime().getTimeInMillis());
		monitoringEvent.setError(error);
	}
	
	public void onLambdaDeletion(AbstractLambda lambda) {
		monitoringEvent.setTime(Calendar.getInstance());
		monitoringEvent.setLambdaName(lambda.getName());
		monitoringEvent.setLambdaOwner(lambda.getOwner());
		monitoringEvent.setStatus(MonitoringEvent.MonitoringEventType.DELETION);
	}
	
	public void onLambdaDeployment(AbstractLambda lambda) {
		monitoringEvent.setTime(Calendar.getInstance());
		monitoringEvent.setLambdaName(lambda.getName());
		monitoringEvent.setLambdaOwner(lambda.getOwner());
		monitoringEvent.setStatus(MonitoringEvent.MonitoringEventType.DEPLOYMENT);
	}
}
