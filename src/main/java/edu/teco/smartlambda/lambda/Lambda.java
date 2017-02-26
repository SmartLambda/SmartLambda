package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

@Entity
@Table(name = "Lambda")
public class Lambda extends AbstractLambda {
	
	public static final int PORT = 4_0_0_0_1;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Getter
	private int id;
	
	@Getter
	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@PrimaryKeyJoinColumn
	@Column(name = "owner")
	private User owner;
	
	@Getter
	@Setter
	@Column(name = "name")
	private String name;
	
	@Getter
	@Setter
	@Column(name = "async")
	private boolean async;
	
	@Column(name = "runtime")
	private String runtime;
	
	private String containerId;
	
	@Transient
	private ContainerBuilder builder = BuilderFactory.getContainerBuilder();
	
	private Session session = Application.getInstance().getSessionFactory().getCurrentSession();
	
	@Override
	public Optional<ExecutionReturnValue> execute(final String params, final boolean async) {
		final ListenableFuture<ExecutionReturnValue> future = ThreadManager.getExecutorService().submit(() -> {
			final Container container = ContainerFactory.getContainerById(containerId);
			try {
				container.start();
			} catch (Exception e) {
				throw (new RuntimeException(e));
			}
			final Gson                 gson = new GsonBuilder().create();
			final Socket               socket;
			final DataInputStream      inputStream;
			final ExecutionReturnValue returnValue;
			
			socket = new Socket("localhost", PORT);
			inputStream = new DataInputStream(socket.getInputStream());
			returnValue = gson.fromJson(inputStream.readUTF(), ExecutionReturnValue.class);
			
			return returnValue;
		});
		
		if (async) {
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
			return Optional.empty();
		} else {
			try {
				return Optional.of(future.get());
			} catch (InterruptedException | ExecutionException e) {
				throw (new RuntimeException(e));
			}
		}
	}
	
	@Override
	public void save() {
		try {
			Container container = builder.build();
			containerId = container.getContainerId();
		} catch (Exception e) {
			throw (new RuntimeException(e));
		}
		session.beginTransaction();
		session.save(this);
		session.getTransaction().commit();
	}
	
	@Override
	public void update() {
		//// FIXME: 2/15/17 
	}
	
	@Override
	public void delete() {
		AbstractLambda lambda = session.load(Lambda.class, id);
		session.delete(lambda);
	}
	
	@Override
	public void schedule(final Event event) {
		//// FIXME: 2/15/17 
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
		//// FIXME: 2/15/17 
		return null;
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		//// FIXME: 2/15/17 
		return null;
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		MonitoringEvent query = from(MonitoringEvent.class);
		where(query.getLambdaName()).eq(name).and(query.getLambdaOwner()).eq(owner);
		
		return select(query).list(session);
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
