package edu.teco.smartlambda.lambda;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.container.BuilderFactory;
import edu.teco.smartlambda.container.Container;
import edu.teco.smartlambda.container.Image;
import edu.teco.smartlambda.container.ImageBuilder;
import edu.teco.smartlambda.container.ImageFactory;
import edu.teco.smartlambda.runtime.ExecutionResult;
import edu.teco.smartlambda.runtime.Runtime;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import edu.teco.smartlambda.schedule.Event;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LambdaFacade.class, RuntimeRegistry.class, Application.class, ImageFactory.class, BuilderFactory.class})
public class LambdaTest {
	
	private Lambda lambda;
	private User   user;
	private static final String LAMBDA_NAME = "myLambda";
	private Runtime runtime;
	
	@Before
	public void setUp() {
		
		this.lambda = new Lambda();
		this.user = mock(User.class);
		this.lambda.setName(LAMBDA_NAME);
		this.lambda.setOwner(this.user);
		this.runtime = mock(Runtime.class);
		when(this.runtime.getName()).thenReturn("runtime");
		this.lambda.setRuntime(this.runtime);
		
	}
	
	
	@Test
	public void execute() throws Exception {
		final boolean[] hasInvoked = new boolean[1];
		final Field field = Lambda.class.getDeclaredField("containerId");
		field.setAccessible(true);
		field.set(this.lambda, "tollerContainer");
		final Container container = mock(Container.class);
		mockStatic(ImageFactory.class);
		final Image image = mock(Image.class);
		when(ImageFactory.getImageById("tollerContainer")).thenReturn(image);
		when(image.start()).thenAnswer(invocation -> {
			hasInvoked[0] = true;
			return container;
		});
		final OutputStream          channel    = mock(OutputStream.class);
		final ByteArrayOutputStream byteStream = mock(ByteArrayOutputStream.class);
		final DataOutputStream      stream     = mock(DataOutputStream.class);
		when(container.getStdIn()).thenReturn(channel);
		PowerMockito.whenNew(ByteArrayOutputStream.class).withAnyArguments().thenReturn(byteStream);
		PowerMockito.whenNew(DataOutputStream.class).withAnyArguments().thenReturn(stream);
		final ExecutionResult result = mock(ExecutionResult.class);
		whenNew(ExecutionResult.class).withAnyArguments().thenReturn(result);
		
		this.lambda.executeSync("lambda");
		this.lambda.executeAsync("lambda").get();
		assertTrue(hasInvoked[0]);
		
	}
	
	
	
	@Test(expected=DuplicateLambdaException.class)
	public void saveDuplicate() throws Exception {
		mockStatic(LambdaFacade.class);
		final LambdaFacade  mockedFacade  = mock(LambdaFacade.class);
		final LambdaFactory mockedFactory = mock(LambdaFactory.class);
		when(LambdaFacade.getInstance()).thenReturn(mockedFacade);
		when(mockedFacade.getFactory()).thenReturn(mockedFactory);
		when(mockedFactory.getLambdaByOwnerAndName(this.user, LAMBDA_NAME)).thenReturn(Optional.of(this.lambda));
		
		final Lambda lambda2 = new Lambda();
		lambda2.setName(LAMBDA_NAME);
		lambda2.setOwner(this.user);
		lambda2.save();
	}
	
	@Test
	public void save() throws Exception {
		mockStatic(LambdaFacade.class);
		final LambdaFacade  mockedFacade  = mock(LambdaFacade.class);
		final LambdaFactory mockedFactory = mock(LambdaFactory.class);
		when(LambdaFacade.getInstance()).thenReturn(mockedFacade);
		when(mockedFacade.getFactory()).thenReturn(mockedFactory);
		when(mockedFactory.getLambdaByOwnerAndName(this.user, LAMBDA_NAME)).thenReturn(Optional.empty());
		
		mockStatic(RuntimeRegistry.class);
		final RuntimeRegistry registry = mock(RuntimeRegistry.class);
		when(RuntimeRegistry.getInstance()).thenReturn(registry);
		when(registry.getRuntimeByName(any(String.class))).thenReturn(this.runtime);
		final Field field = Lambda.class.getDeclaredField("builder");
		field.setAccessible(true);
		final ImageBuilder builder = mock(ImageBuilder.class);
		field.set(this.lambda, builder);
		
		final Image image = mock(Image.class);
		when(builder.build()).thenReturn(image);
		when(image.getId()).thenReturn("1234");
		
		mockStatic(Application.class);
		final Application    application = mock(Application.class);
		final SessionFactory factory     = mock(SessionFactory.class);
		final Session        session     = mock(Session.class);
		when(Application.getInstance()).thenReturn(application);
		when(application.getSessionFactory()).thenReturn(factory);
		when(factory.getCurrentSession()).thenReturn(session);
		
		this.lambda.save();
		verify(builder).build();
		verify(session).save(this.lambda);
		
	}
	
	@Test
	public void update() throws Exception {
		
		final Field field = Lambda.class.getDeclaredField("builder");
		field.setAccessible(true);
		final ImageBuilder builder = mock(ImageBuilder.class);
		field.set(this.lambda, builder);
		
		mockStatic(ImageFactory.class);
		final Image image = mock(Image.class);
		when(ImageFactory.getImageById(null)).thenReturn(image);
		
		mockStatic(RuntimeRegistry.class);
		final RuntimeRegistry registry = mock(RuntimeRegistry.class);
		when(RuntimeRegistry.getInstance()).thenReturn(registry);
		when(registry.getRuntimeByName(any(String.class))).thenReturn(this.runtime);
		
		when(builder.build()).thenReturn(image);
		when(image.getId()).thenReturn("1234");
		
		mockStatic(Application.class);
		final Application    application = mock(Application.class);
		final SessionFactory factory     = mock(SessionFactory.class);
		final Session        session     = mock(Session.class);
		when(Application.getInstance()).thenReturn(application);
		when(application.getSessionFactory()).thenReturn(factory);
		when(factory.getCurrentSession()).thenReturn(session);
		
		this.lambda.update();
		verify(builder).build();
		verify(session).update(this.lambda);
		
	}
	
	@Test
	public void delete() throws Exception {
		mockStatic(Application.class);
		final Application    application    = mock(Application.class);
		final SessionFactory sessionFactory = mock(SessionFactory.class);
		final Session        currentSession = mock(Session.class);
		
		when(Application.getInstance()).thenReturn(application);
		when(application.getSessionFactory()).thenReturn(sessionFactory);
		when(sessionFactory.getCurrentSession()).thenReturn(currentSession);
		mockStatic(ImageFactory.class);
		final Image image = mock(Image.class);
		when(ImageFactory.getImageById(null)).thenReturn(image);
		
		this.lambda.delete();
		verify(currentSession).delete(this.lambda);
		verify(image).delete();
	}
	
	
	@Test(expected=DuplicateEventException.class)
	public void scheduleDuplicate() throws Exception {
				
		final Lambda          mockedLambda = mock(Lambda.class);
		final Optional<Event> event        = Optional.of(mock(Event.class));
		when(mockedLambda.getScheduledEvent(any(String.class))).thenReturn(event);
		
		final Event event2 = mock(Event.class);
		when(event2.getName()).thenReturn("event");
		doCallRealMethod().when(mockedLambda).schedule(event2);
		mockedLambda.schedule(event2);
	}
	
	@Test
	public void schedule() throws Exception {
		
	}
	
	@Test
	public void deployBinary() throws Exception {
		final Field field = Lambda.class.getDeclaredField("builder");
		field.setAccessible(true);
		final ImageBuilder builder = mock(ImageBuilder.class);
		field.set(this.lambda, builder);
		mockStatic(BuilderFactory.class);
		when(BuilderFactory.getContainerBuilder()).thenReturn(builder);
		mockStatic(RuntimeRegistry.class);
		final RuntimeRegistry registry = mock(RuntimeRegistry.class);
		when(RuntimeRegistry.getInstance()).thenReturn(registry);
		when(registry.getRuntimeByName("runtime")).thenReturn(this.runtime);
		when(this.runtime.getBinaryName()).thenReturn("binaryName");
		final byte[] content = {'a'};
		this.lambda.deployBinary(content);
		verify(builder).storeFile(content, "binaryName");
		
	}
			
}