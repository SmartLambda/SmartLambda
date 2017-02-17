package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.lambda.AbstractLambda;
import lombok.Getter;


import java.util.Calendar;
import java.util.function.Supplier;

/**
 * Created by Melanie on 29.01.2017.
 */
	
public class MonitoringService {
	
	private ThreadLocal<MonitoringService> instance;
	@Getter
	private MonitoringEvent                monitoringEvent;
	private AuthenticationService authenticationService = AuthenticationService.getInstance();
	
	public MonitoringService() {}
	
	public void onLambdaExecutionStart(final AbstractLambda lambda) {
		monitoringEvent = new MonitoringEvent(lambda.getOwner(), lambda.getName(), MonitoringEvent.MonitoringEventType.EXECUTION,
				authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
		monitoringEvent.setTime(Calendar.getInstance());
	}
	
	public void onLambdaExecutionEnd(final AbstractLambda lambda, final int CPUTime) {
		monitoringEvent.setCPUTime(CPUTime);
		monitoringEvent.setDuration(Calendar.getInstance().getTimeInMillis() - monitoringEvent.getTime().getTimeInMillis());
	}
	
	public void onLambdaExecutionEnd(final AbstractLambda lambda, final int CPUTime, final String error) {
		monitoringEvent.setCPUTime(CPUTime);
		monitoringEvent.setDuration(Calendar.getInstance().getTimeInMillis() - monitoringEvent.getTime().getTimeInMillis());
		monitoringEvent.setError(error);
	}
	
	public void onLambdaDeletion(final AbstractLambda lambda) {
		monitoringEvent = new MonitoringEvent(lambda.getOwner(), lambda.getName(), MonitoringEvent.MonitoringEventType.DELETION,
				authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
		monitoringEvent.setTime(Calendar.getInstance());
	}
	
	public void onLambdaDeployment(final AbstractLambda lambda) {
		monitoringEvent = new MonitoringEvent(lambda.getOwner(), lambda.getName(), MonitoringEvent.MonitoringEventType.DEPLOYMENT,
				authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
		monitoringEvent.setTime(Calendar.getInstance());
	}
}
