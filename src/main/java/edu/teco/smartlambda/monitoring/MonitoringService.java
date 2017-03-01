package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.shared.ExecutionReturnValue;

import java.util.Calendar;

/**
 * Created by Melanie on 29.01.2017.
 */

public class MonitoringService {
	
	private static ThreadLocal<MonitoringService> instance;
	private        MonitoringEvent                monitoringEvent;
	private AuthenticationService authenticationService = AuthenticationService.getInstance();
	
	public MonitoringService() {}
	
	public static MonitoringService getInstance() {
		if (instance == null) {
			instance = new ThreadLocal<>();
			instance.set(new MonitoringService());
		} else if (instance.get() == null) {
			instance.set(new MonitoringService());
		}
		
		return instance.get();
	}
	
	public void onLambdaExecutionStart(final AbstractLambda lambda) {
		monitoringEvent = new MonitoringEvent(lambda,
				MonitoringEvent.MonitoringEventType.EXECUTION,
				authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
	}
	
	public void onLambdaExecutionEnd(final AbstractLambda lambda, final int CPUTime, ExecutionReturnValue executionReturnValue) {
		monitoringEvent.setCPUTime(CPUTime);
		monitoringEvent.setDuration(Calendar.getInstance().getTimeInMillis() - monitoringEvent.getTime().getTimeInMillis());
		if (executionReturnValue.isException()) {
			monitoringEvent.setError(executionReturnValue.getException().get().getStackTrace().toString());
		}
		monitoringEvent.save();
	}
	
	public void onLambdaDeletion(final AbstractLambda lambda) {
		monitoringEvent = new MonitoringEvent(lambda,
				MonitoringEvent.MonitoringEventType.DELETION,
				authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
		monitoringEvent.save();
	}
	
	public void onLambdaDeployment(final AbstractLambda lambda) {
		monitoringEvent = new MonitoringEvent(lambda,
				MonitoringEvent.MonitoringEventType.DEPLOYMENT,
				authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
		monitoringEvent.save();
	}
}
