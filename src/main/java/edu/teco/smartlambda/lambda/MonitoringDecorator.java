package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.monitoring.MonitoringService;
import edu.teco.smartlambda.schedule.Event;

import java.util.List;
import java.util.Optional;

/**
 * Decorates lambdas with calls to the monitoring service
 */

public class MonitoringDecorator extends LambdaDecorator {
	
	public MonitoringDecorator(final AbstractLambda lambda) {
		super(lambda);
	}
	
	@Override
	public Optional<ExecutionReturnValue> execute(final String params, final boolean async) {
		if(!async) {
			MonitoringService.getInstance().onLambdaExecutionStart(lambda);
			Optional<ExecutionReturnValue> returnVal = super.execute(params, async);
			if(!returnVal.isPresent() || !returnVal.get().isException()) {
				//TODO: get CPUTime
				MonitoringService.getInstance().onLambdaExecutionEnd(lambda, 0);
			} else {
				//TODO: get CPUTime
				MonitoringService.getInstance().onLambdaExecutionEnd(lambda, 0, returnVal.get().getException().get().getStackTrace().toString());
			}
			return returnVal;
		} else {
			MonitoringService.getInstance().onLambdaExecutionStart(lambda);
			return super.execute(params, async);
		}
	}
	
	@Override
	public void save() {
		MonitoringService.getInstance().onLambdaDeployment(lambda);
		super.save();
	}
	
	@Override
	public void update() {
		super.update();
	}
	
	@Override
	public void delete() {
		MonitoringService.getInstance().onLambdaDeletion(lambda);
		super.delete();
	}
	
	@Override
	public void schedule(final Event event) {
		super.schedule(event);
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		super.deployBinary(content);
	}
	
	@Override
	public Event getScheduledEvent(final String name) {
		return super.getScheduledEvent(name);
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		return super.getScheduledEvents();
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		return super.getMonitoringEvents();
	}
}
