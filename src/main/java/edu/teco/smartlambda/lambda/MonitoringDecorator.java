package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.monitoring.MonitoringService;
import edu.teco.smartlambda.runtime.ExecutionResult;
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
	public Optional<ExecutionResult> executeSync(final String params) {
		MonitoringService.getInstance().onLambdaExecutionStart(this.lambda);
		final Optional<ExecutionResult> returnVal = super.executeSync(params);
		MonitoringService.getInstance()
				.onLambdaExecutionEnd(this.lambda, returnVal.get().getConsumedCPUTime(), returnVal.get().getExecutionReturnValue());
		
		return returnVal;
	}
	
	@Override
	public ListenableFuture<ExecutionResult> executeAsync(final String params) {
		MonitoringService.getInstance().onLambdaExecutionStart(this.lambda);
		return super.executeAsync(params);
	}
	
	@Override
	public void save() {
		MonitoringService.getInstance().onLambdaDeployment(this.lambda);
		super.save();
	}
	
	@Override
	public void update() {
		super.update();
	}
	
	@Override
	public void delete() {
		MonitoringService.getInstance().onLambdaDeletion(this.lambda);
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
