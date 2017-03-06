package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.ListenableFuture;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.PermissionType;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.schedule.Event;
import edu.teco.smartlambda.shared.ExecutionReturnValue;

import java.util.List;
import java.util.Optional;

/**
 * Decorates lambdas with authentication and aborts the lambda call, if authentication fails
 */
public class PermissionDecorator extends LambdaDecorator {
	
	private Lambda unwrappedLambda = LambdaDecorator.unwrap(this.lambda);
	
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
				.hasPermission(this.unwrappedLambda, type)) throw new InsufficientPermissionsException();
	}
	
	@Override
	public Optional<ExecutionReturnValue> executeSync(final String params) {
		this.ensureActionIsPermitted(PermissionType.EXECUTE);
		return super.executeSync(params);
	}
	
	@Override
	public ListenableFuture<ExecutionReturnValue> executeAsync(final String params) {
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
		this.ensureActionIsPermitted(PermissionType.EXECUTE);
		super.schedule(event);
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		this.ensureActionIsPermitted(this.unwrappedLambda.getId() == 0 ? PermissionType.CREATE : PermissionType.PATCH);
		super.deployBinary(content);
	}
	
	@Override
	public Event getScheduledEvent(final String name) {
		this.ensureActionIsPermitted(PermissionType.SCHEDULE);
		return super.getScheduledEvent(name);
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		this.ensureActionIsPermitted(PermissionType.SCHEDULE);
		return super.getScheduledEvents();
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		this.ensureActionIsPermitted(PermissionType.STATUS);
		return super.getMonitoringEvents();
	}
}
