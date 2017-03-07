package edu.teco.smartlambda.monitoring;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.lambda.AbstractLambda;
import edu.teco.smartlambda.shared.ExecutionReturnValue;

import java.util.Calendar;

/**
 *
 */

public class MonitoringService {
	
	private static ThreadLocal<MonitoringService> instance;
	private final AuthenticationService authenticationService = AuthenticationService.getInstance();
	
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
	
	/**
	 * Sets properties in event that are known at start of execution
	 *
	 * @param lambda monitored lambda
	 */
	public MonitoringEvent onLambdaExecutionStart(final AbstractLambda lambda) {
		return new MonitoringEvent(lambda, MonitoringEvent.MonitoringEventType.EXECUTION,
				this.authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
	}
	
	/**
	 * Sets the missing properties in event and saves the event to the database
	 *  @param lambda               monitored lambda
	 * @param CPUTime              the lambda used
	 * @param executionReturnValue executionReturnValue or exception of lambda
	 * @param monitoringEvent
	 */
	public void onLambdaExecutionEnd(final AbstractLambda lambda, final long CPUTime, final ExecutionReturnValue executionReturnValue,
			final MonitoringEvent monitoringEvent) {
		monitoringEvent.setCPUTime(CPUTime);
		monitoringEvent.setDuration(Calendar.getInstance().getTimeInMillis() - monitoringEvent.getTime().getTimeInMillis());
		if (executionReturnValue.isException()) {
			monitoringEvent.setError(executionReturnValue.getException().get().getStackTrace().toString());
		}
		monitoringEvent.save();
	}
	
	/**
	 * Sets properties of event that are important at deletion
	 *
	 * @param lambda monitored lambda
	 */
	public void onLambdaDeletion(final AbstractLambda lambda) {
		final MonitoringEvent monitoringEvent = new MonitoringEvent(lambda, MonitoringEvent.MonitoringEventType.DELETION,
				this.authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
		monitoringEvent.save();
	}
	
	/**
	 * Sets properties of event that are important at deployment
	 *
	 * @param lambda monitored lambda
	 */
	public void onLambdaDeployment(final AbstractLambda lambda) {
		final MonitoringEvent monitoringEvent = new MonitoringEvent(lambda, MonitoringEvent.MonitoringEventType.DEPLOYMENT,
				this.authenticationService.getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new));
		monitoringEvent.save();
	}
}
