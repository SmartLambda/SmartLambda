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
 * Decorates lambdas with authenticagtion and aborts the lambda call, if authentication fails
 */
//// FIXME: 2/15/17 
public class PermissionDecorator extends LambdaDecorator {
	
	private Lambda unwrappedLambda = LambdaDecorator.unwrap(lambda);
	
	public PermissionDecorator(final AbstractLambda lambda) {
		super(lambda);
	}
	
	private boolean authenticatedKeyHasPermissionOnThisLambda(PermissionType type) {
		return AuthenticationService.getInstance().getAuthenticatedKey().orElseThrow(NotAuthenticatedException::new).hasPermission
				(unwrappedLambda, type);
	}
	
	@Override
	public Optional<ExecutionReturnValue> executeSync(final String params) {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.EXECUTE)) throw new InsufficientPermissionsException();
		return super.executeSync(params);
	}
	
	@Override
	public ListenableFuture<ExecutionReturnValue> executeAsync(final String params) {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.EXECUTE)) throw new InsufficientPermissionsException();
		return super.executeAsync(params);
	}
	
	@Override
	public void save() {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.CREATE)) throw new InsufficientPermissionsException();
		super.save();
	}
	
	@Override
	public void update() {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.PATCH)) throw new InsufficientPermissionsException();
		super.update();
	}
	
	@Override
	public void delete() {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.DELETE)) throw new InsufficientPermissionsException();
		super.delete();
	}
	
	@Override
	public void schedule(final Event event) {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.SCHEDULE)) throw new InsufficientPermissionsException();
		super.schedule(event);
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		if (!((unwrappedLambda.getId()==0 &&
			   authenticatedKeyHasPermissionOnThisLambda(PermissionType.CREATE)
		      ) ||
			  (unwrappedLambda.getId()!=0 &&
			   authenticatedKeyHasPermissionOnThisLambda(PermissionType.PATCH)
			  )
		     )) throw new InsufficientPermissionsException();
		super.deployBinary(content);
	}
	
	@Override
	public Event getScheduledEvent(final String name) {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.SCHEDULE)) throw new InsufficientPermissionsException();
		return super.getScheduledEvent(name);
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.SCHEDULE)) throw new InsufficientPermissionsException();
		return super.getScheduledEvents();
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		if (!authenticatedKeyHasPermissionOnThisLambda(PermissionType.STATUS)) throw new InsufficientPermissionsException();
		return super.getMonitoringEvents();
	}
}
