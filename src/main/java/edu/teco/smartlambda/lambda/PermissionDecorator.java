package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.PermissionType;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.runtime.ExecutionResult;
import edu.teco.smartlambda.schedule.Event;

import java.util.List;

/**
 * Decorates lambdas with authentication and aborts the lambda call, if authentication fails
 */
public class PermissionDecorator extends LambdaDecorator {
	
	public PermissionDecorator(final AbstractLambda lambda) {
		super(lambda);
	}
	
	/**
	 * Ensure the requester has the permission to perform the action and throw an exception if not
	 *
	 * @param type required permission type
	 */
	private void ensureActionIsPermitted(final PermissionType type) {
		if (!AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new)
				.hasPermission(LambdaDecorator.unwrap(this.lambda), type)) throw new InsufficientPermissionsException();
	}
	
	@Override
	public ExecutionResult executeSync(final String params) {
		this.ensureActionIsPermitted(PermissionType.EXECUTE);
		return super.executeSync(params);
	}
	
	@Override
	public ListenableFuture<ExecutionResult> executeAsync(final String params) {
		this.ensureActionIsPermitted(PermissionType.EXECUTE);
		return super.executeAsync(params);
	}
	
	@Override
	public void save() {
		this.ensureActionIsPermitted(PermissionType.CREATE);
		super.save();
	}
	
	@Override
	public void update() {
		this.ensureActionIsPermitted(PermissionType.PATCH);
		super.update();
	}
	
	@Override
	public void delete() {
		this.ensureActionIsPermitted(PermissionType.DELETE);
		super.delete();
	}
	
	@Override
	public void schedule(final Event event) {
		this.ensureActionIsPermitted(PermissionType.SCHEDULE);
		this.ensureActionIsPermitted(PermissionType.EXECUTE);
		super.schedule(event);
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		this.ensureActionIsPermitted(LambdaDecorator.unwrap(this.lambda).getId() == 0 ? PermissionType.CREATE : PermissionType.PATCH);
		super.deployBinary(content);
	}
	
	@Override
	public Event getScheduledEvent(final String name) {
		this.ensureActionIsPermitted(PermissionType.SCHEDULE);
		final Event event = super.getScheduledEvent(name);
		
		if (AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new) !=
				event.getKey().getUser() || !event.getKey().isPrimaryKey()) this.ensureActionIsPermitted(PermissionType.READ);
		
		return event;
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		this.ensureActionIsPermitted(PermissionType.SCHEDULE);
		this.ensureActionIsPermitted(PermissionType.READ);
		return super.getScheduledEvents();
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		this.ensureActionIsPermitted(PermissionType.STATUS);
		return super.getMonitoringEvents();
	}
}
