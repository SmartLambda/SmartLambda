package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.GsonBuilder;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.concurrent.ThreadManager;
import edu.teco.smartlambda.container.BuilderFactory;
import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.ContainerBuilder;
import edu.teco.smartlambda.container.ContainerFactory;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.monitoring.MonitoringService;
import edu.teco.smartlambda.runtime.Runtime;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import edu.teco.smartlambda.schedule.Event;
import edu.teco.smartlambda.shared.ExecutionReturnValue;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.output.NullOutputStream;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

@Entity
@Table(name = "Lambda")
public class Lambda extends AbstractLambda {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Getter
	private int id;
	
	@Getter
	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner")
	private User owner;
	
	@Getter
	@Setter
	private String name;
	
	@Getter
	@Setter
	private boolean async;
	
	private String runtime;
	
	private String containerId;
	
	@Transient
	private ContainerBuilder builder = BuilderFactory.getContainerBuilder();
	
	private static final int MAX_RETRIES = 4;
	
	@Override
	public Optional<ExecutionReturnValue> executeSync(final String params) {
		final ListenableFuture<ExecutionReturnValue> future = execute(params);
		try {
			return Optional.of(future.get());
		} catch (InterruptedException | ExecutionException e) {
			throw (new RuntimeException(e));
		}
	}
	
	@Override
	public ListenableFuture<ExecutionReturnValue> executeAsync(final String params) {
		final ListenableFuture<ExecutionReturnValue> future = execute(params);
		Futures.addCallback(future, new FutureCallback<ExecutionReturnValue>() {
			//TODO: CPUTime!
			@Override
			public void onSuccess(final ExecutionReturnValue result) {
				MonitoringService.getInstance().onLambdaExecutionEnd(Lambda.this, 0, result);
			}
			
			@Override
			public void onFailure(final Throwable t) {
				MonitoringService.getInstance().onLambdaExecutionEnd(Lambda.this, 0, new ExecutionReturnValue(null, t));
			}
		});
		return future;
	}
	
	private ListenableFuture<ExecutionReturnValue> execute(final String params) {
		return ThreadManager.getExecutorService().submit(() -> {
			final Container           container = ContainerFactory.getContainerById(containerId);
			final WritableByteChannel stdIn;
			
			try {
				stdIn = container.start();
			} catch (Exception e) {
				throw (new RuntimeException(e));
			}
			
			final ByteArrayOutputStream byteBufferStream       = new ByteArrayOutputStream(params.length() + 4);
			final DataOutputStream      byteBufferStreamFiller = new DataOutputStream(byteBufferStream);
			byteBufferStreamFiller.writeInt(params.length());
			byteBufferStreamFiller.write(params.getBytes());
			byteBufferStreamFiller.flush();
			byteBufferStreamFiller.close();
			stdIn.write(ByteBuffer.wrap(byteBufferStream.toByteArray()));
			
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			container.attach(outputStream, new NullOutputStream());
			
			return new GsonBuilder().create().fromJson(new String(outputStream.toByteArray()), ExecutionReturnValue.class);
		});
	}
	
	@Override
	public void save() {
		if (LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(owner, name).isPresent())
			throw new DuplicateLambdaException(owner, name);
		
		try {
			RuntimeRegistry.getInstance().getRuntimeByName(this.runtime).setupContainerImage(builder);
			final Container container = builder.build();
			containerId = container.getContainerId();
		} catch (Exception e) {
			throw (new RuntimeException(e));
		}
		
		Application.getInstance().getSessionFactory().getCurrentSession().save(this);
	}
	
	@Override
	public void update() {
		//// FIXME: 2/15/17 
	}
	
	@Override
	public void delete() {
		Application.getInstance().getSessionFactory().getCurrentSession().delete(this);
		try {
			ContainerFactory.getContainerById(this.containerId).delete();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void schedule(final Event event) {
		event.setLambda(this);
		event.save();
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		try {
			builder.storeFile(content, RuntimeRegistry.getInstance().getRuntimeByName(this.runtime).getBinaryName());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Event getScheduledEvent(final String name) {
		Event query = from(Event.class);
		where(query.getLambda()).eq(this);
		return select(query).setMaxResults(1).get(Application.getInstance().getSessionFactory().getCurrentSession()).get();
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		Event query = from(Event.class);
		where(query.getLambda()).eq(this);
		
		return select(query).list(Application.getInstance().getSessionFactory().getCurrentSession());
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		MonitoringEvent query = from(MonitoringEvent.class);
		where(query.getLambdaName()).eq(name).and(query.getLambdaOwner()).eq(owner);
		
		return select(query).list(Application.getInstance().getSessionFactory().getCurrentSession());
	}
	
	@Override
	public Runtime getRuntime() {
		return RuntimeRegistry.getInstance().getRuntimeByName(this.runtime);
	}
	
	@Override
	public void setRuntime(final Runtime runtime) {
		this.runtime = runtime.getName();
	}
}