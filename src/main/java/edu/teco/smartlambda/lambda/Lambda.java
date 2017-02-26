package edu.teco.smartlambda.lambda;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.container.BuilderFactory;
import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.ContainerBuilder;
import edu.teco.smartlambda.container.ContainerFactory;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.runtime.Runtime;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import edu.teco.smartlambda.schedule.Event;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

@Entity
@Table(appliesTo = "Lambda")
public class Lambda extends AbstractLambda {
	
	public static final int PORT = 4_0_0_0_1;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Getter
	@Setter
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
	
	private Session session = Application.getInstance().getSessionFactory().openSession();
	
	@Override
	public Optional<ExecutionReturnValue> execute(final String params, final boolean async) {
		Container container = ContainerFactory.getContainerById(containerId);
		try {
			container.start();
		} catch (Exception e) {
			throw (new RuntimeException(e));
		}
		final Gson   gson = new GsonBuilder().create();
		final Socket socket;
		final DataInputStream inputStream;
		final ExecutionReturnValue returnValue;
		try {
			socket = new Socket("localhost", PORT);
			inputStream = new DataInputStream(socket.getInputStream());
			returnValue = gson.fromJson(inputStream.readUTF(), ExecutionReturnValue.class);
		} catch (Exception e) {
			throw (new RuntimeException(e));
		}
		
		return Optional.of(returnValue);
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
		Object lambda = session.load(Lambda.class, id);
		session.delete(lambda);
	}
	
	@Override
	public void schedule(final Event event) {
		//// FIXME: 2/15/17 
	}
	
	@Override
	public void deployBinary(final byte[] content) {
		//// FIXME: 2/15/17 
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
