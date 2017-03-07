package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.monitoring.MonitoringService;
import edu.teco.smartlambda.runtime.ExecutionResult;
import edu.teco.smartlambda.schedule.Event;
import edu.teco.smartlambda.shared.ExecutionReturnValue;

import java.util.List;

/**
 * Decorates lambdas with calls to the monitoring service
 */

public class MonitoringDecorator extends LambdaDecorator {
	
	public MonitoringDecorator(final AbstractLambda lambda) {
		super(lambda);
	}
	
	@Override
	public ExecutionResult executeSync(final String params) {
		MonitoringService.getInstance().onLambdaExecutionStart(this.lambda);
		final ExecutionResult returnVal = super.executeSync(params);
		MonitoringService.getInstance()
				.onLambdaExecutionEnd(this.lambda, returnVal.getConsumedCPUTime(), returnVal.getExecutionReturnValue());
		
		return returnVal;
	}
	
	@Override
	public ListenableFuture<ExecutionResult> executeAsync(final String params) {
		MonitoringService.getInstance().onLambdaExecutionStart(this.lambda);
		final ListenableFuture<ExecutionResult> future = super.executeAsync(params);
		Futures.addCallback(future, new FutureCallback<ExecutionResult>() {
			@Override
			public void onSuccess(final ExecutionResult result) {
				MonitoringService.getInstance().onLambdaExecutionEnd(MonitoringDecorator.this.lambda, result.getConsumedCPUTime(),
						result.getExecutionReturnValue());
			}
			
			@Override
			public void onFailure(final Throwable t) {
				MonitoringService.getInstance().onLambdaExecutionEnd(MonitoringDecorator.this.lambda, 0, new ExecutionReturnValue(null,
						t));
			}
		});
		
		return future;
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
