package edu.teco.smartlambda.lambda;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.GsonBuilder;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.concurrent.ThreadManager;
import edu.teco.smartlambda.container.BuilderFactory;
import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.Image;
import edu.teco.smartlambda.container.ImageBuilder;
import edu.teco.smartlambda.container.ImageFactory;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.runtime.ExecutionResult;
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
	private ImageBuilder builder = null;
	
	@Override
	public ExecutionResult executeSync(final String params) {
		final ListenableFuture<ExecutionResult> future = this.execute(params);
		try {
			return future.get();
		} catch (final InterruptedException | ExecutionException e) {
			throw (new RuntimeException(e));
		}
	}
	
	@Override
	public ListenableFuture<ExecutionResult> executeAsync(final String params) {
		return this.execute(params);
	}
	
	private ListenableFuture<ExecutionResult> execute(final String params) {
		return ThreadManager.getExecutorService().submit(() -> {
			final Container container;
			
			try {
				container = ImageFactory.getImageById(this.containerId).start();
			} catch (final Exception e) {
				throw (new RuntimeException(e));
			}
			
			final WritableByteChannel   stdIn                  = container.getStdIn();
			final ByteArrayOutputStream byteBufferStream       = new ByteArrayOutputStream(params.length() + 4);
			final DataOutputStream      byteBufferStreamFiller = new DataOutputStream(byteBufferStream);
			byteBufferStreamFiller.writeInt(params.length());
			byteBufferStreamFiller.write(params.getBytes());
			byteBufferStreamFiller.flush();
			byteBufferStreamFiller.close();
			stdIn.write(ByteBuffer.wrap(byteBufferStream.toByteArray()));
			
			final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			container.attach(outputStream, new NullOutputStream());
			
			final ExecutionResult result = new ExecutionResult();
			result.setExecutionReturnValue(
					new GsonBuilder().create().fromJson(new String(outputStream.toByteArray()), ExecutionReturnValue.class));
			result.setConsumedCPUTime(container.getConsumedCPUTime());
			
			return result;
		});
	}
	
	@Override
	public void save() {
		if (LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(this.owner, this.name).isPresent())
			throw new DuplicateLambdaException(this.owner, this.name);
		
		try {
			RuntimeRegistry.getInstance().getRuntimeByName(this.runtime).setupContainerImage(this.builder);
			final Image image = this.builder.build();
			this.containerId = image.getId();
		} catch (final Exception e) {
			throw (new RuntimeException(e));
		}
		
		Application.getInstance().getSessionFactory().getCurrentSession().save(this);
	}
	
	@Override
	public void update() {
		if (this.builder != null) { // if a new binary exists
			try {
				ImageFactory.getImageById(this.containerId).delete(); // delete old image
				
				// deploy new image
				RuntimeRegistry.getInstance().getRuntimeByName(this.runtime).setupContainerImage(this.builder);
				final Image image = this.builder.build();
				this.containerId = image.getId();
			} catch (final Exception e) {
				throw (new RuntimeException(e));
			}
		}
		
		Application.getInstance().getSessionFactory().getCurrentSession().update(this);
	}
	
	@Override
	public void delete() {
		Application.getInstance().getSessionFactory().getCurrentSession().delete(this);
		try {
			ImageFactory.getImageById(this.containerId).delete();
		} catch (final Exception e) {
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
			this.builder = BuilderFactory.getContainerBuilder();
			this.builder.storeFile(content, RuntimeRegistry.getInstance().getRuntimeByName(this.runtime).getBinaryName());
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public Optional<Event> getScheduledEvent(final String name) {
		final Event query = from(Event.class);
		where(query.getLambda()).eq(this);
		return select(query).setMaxResults(1).get(Application.getInstance().getSessionFactory().getCurrentSession());
	}
	
	@Override
	public List<Event> getScheduledEvents() {
		final Event query = from(Event.class);
		where(query.getLambda()).eq(this);
		
		return select(query).list(Application.getInstance().getSessionFactory().getCurrentSession());
	}
	
	@Override
	public List<MonitoringEvent> getMonitoringEvents() {
		final MonitoringEvent query = from(MonitoringEvent.class);
		where(query.getLambdaName()).eq(this.name).and(query.getLambdaOwner()).eq(this.owner);
		
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