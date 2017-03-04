package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.runtime.Runtime;
import edu.teco.smartlambda.schedule.Event;
import edu.teco.smartlambda.shared.ExecutionReturnValue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * An abstract superclass to a lambda representation, that defines the interface for lambdas and their decorators
 */
public abstract class AbstractLambda {
	
	public abstract Optional<ExecutionReturnValue> executeSync(final String params);
	
	public abstract Future<ExecutionReturnValue> executeAsync(final String params);
	
	/**
	 * Saves the lambda object into the database
	 */
	public abstract void save();
	
	/**
	 * Saves changes of the lambda into the database
	 */
	public abstract void update();
	
	/**
	 * Deletes the lambda from database
	 */
	public abstract void delete();
	
	/**
	 * Schedules an execution defined by the given event
	 *
	 * @param event scheduling event
	 */
	public abstract void schedule(final Event event);
	
	/**
	 * Deploy the lambda binary
	 *
	 * @param content byte array representing the binary content
	 */
	public abstract void deployBinary(final byte[] content);
	
	/**
	 * Get a scheduled event by its name
	 *
	 * @param name name of requested event
	 *
	 * @return the scheduled event or null if none such exists
	 */
	public abstract Event getScheduledEvent(final String name);
	
	/**
	 * @return a list of all scheduled events
	 */
	public abstract List<Event> getScheduledEvents();
	
	/**
	 * @return a list of all logged monitoring events
	 */
	public abstract List<MonitoringEvent> getMonitoringEvents();
	
	/**
	 * @return name of the lambda
	 */
	public abstract String getName();
	
	/**
	 * @return owner of the lambda
	 */
	public abstract User getOwner();
	
	/**
	 * @return whether the lambda should be executed asynchronously by default
	 */
	public abstract boolean isAsync();
	
	/**
	 * @return the runtime of the lambda
	 */
	public abstract Runtime getRuntime();
	
	/**
	 * Set the name of the lambda
	 *
	 * @param name lambda name
	 */
	public abstract void setName(final String name);
	
	/**
	 * Set the owner of the lambda
	 *
	 * @param owner lambda owner
	 */
	public abstract void setOwner(final User owner);
	
	/**
	 * Set whether this lambda shall be executed asynchronously by default
	 *
	 * @param async true, if this lambda shall be executed async by default, false otherwise
	 */
	public abstract void setAsync(final boolean async);
	
	/**
	 * Sets the runtime to be used for executing the lambda
	 *
	 * @param runtime lambda runtime
	 */
	public abstract void setRuntime(final Runtime runtime);
}
