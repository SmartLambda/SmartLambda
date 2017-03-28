package edu.teco.smartlambda;

import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.Permission;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.identity.GitHubCredential;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import edu.teco.smartlambda.schedule.Event;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import spark.Spark;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

/**
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Application.class, Configuration.class, Spark.class, RuntimeRegistry.class, IdentityProviderRegistry.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING) // ignored by PowerMockRunner so far
public class ApplicationTest {
	
	private        Configuration  configuration;
	private static SessionFactory sessionFactory;
	
	@BeforeClass
	public static void beforeClass() {
		sessionFactory = mock(SessionFactory.class);
	}
	
	@Before
	public void setUp() throws Exception {
		whenNew(Configuration.class).withAnyArguments().thenReturn(this.configuration = mock(Configuration.class));
		when(this.configuration.buildSessionFactory()).thenReturn(sessionFactory);
	}
	
	/*
	 Workaround: Call all tests from within a single one, since PowerMockRunnter ignores the fixed method order
	 */
	@Test
	public void testAll() throws Exception {
		this.getInstance();
		this.getSessionFactory();
		this.main();
	}
	
	public void getInstance() throws Exception {
		assertNotNull(Application.getInstance());
		assertSame(Application.getInstance(), Application.getInstance());
		
		// verify that all entity classes are registered
		verify(this.configuration).addAnnotatedClass(Key.class);
		verify(this.configuration).addAnnotatedClass(User.class);
		verify(this.configuration).addAnnotatedClass(Permission.class);
		verify(this.configuration).addAnnotatedClass(Lambda.class);
		verify(this.configuration).addAnnotatedClass(MonitoringEvent.class);
		verify(this.configuration).addAnnotatedClass(Event.class);
		verify(this.configuration).addAnnotatedClass(GitHubCredential.class);
		
		// verify that the session factory is initialized
		verify(this.configuration).buildSessionFactory();
	}
	
	public void getSessionFactory() throws Exception {
		// verify, that the session factory is always the same
		assertSame(sessionFactory, Application.getInstance().getSessionFactory());
	}
	
	public void main() throws Exception {
		mockStatic(Spark.class);
		mockStatic(RuntimeRegistry.class);
		mockStatic(IdentityProviderRegistry.class);
		
		Application.main();
		
		// verify that spark was started
		verifyStatic();
		Spark.port(ArgumentMatchers.anyInt());
		
		// verify that runtimes are loaded
		verifyStatic();
		RuntimeRegistry.getInstance();
		
		// verify that identity providers are loaded
		verifyStatic();
		IdentityProviderRegistry.getInstance();
	}
}