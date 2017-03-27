package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.torpedoquery.jpa.OnGoingStringCondition;
import org.torpedoquery.jpa.Query;
import org.torpedoquery.jpa.Torpedo;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created on 22.03.17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({User.class, Torpedo.class, Application.class})
public class GitHubIdentityProviderTest {
	
	private String                 createUserAnswer;
	private Class                  torpedoFromAnswer;
	private String                 torpedoWhereAnswer;
	private Object                 sessionSaveAnswer;
	private Session                session;
	private OnGoingStringCondition onGoingStringCondition;
	private GitHubCredential       gitHubCredential;
	
	private GitHubIdentityProvider gitHubIdentityProvider;
	
	@Before
	public void setUp() throws Exception {
		this.gitHubIdentityProvider = new GitHubIdentityProvider();
		
		//Don't create a new User but check the supplied name
		PowerMockito.mockStatic(User.class);
		PowerMockito.when(User.createUser(anyString())).thenAnswer(invocation -> {
			this.createUserAnswer = (String) invocation.getArguments()[0];
			return null;
		});
		
		//Catch and test Torpedoquery invocations
		GitHubCredential gitHubCredential = PowerMockito.mock(GitHubCredential.class);
		PowerMockito.when(gitHubCredential.getAccessToken()).thenReturn("mockedAccessToken");
		
		PowerMockito.mockStatic(Torpedo.class);
		PowerMockito.when(Torpedo.from(GitHubCredential.class)).thenReturn(gitHubCredential);
		PowerMockito.when(Torpedo.where(anyString())).thenAnswer(invocation -> {
			this.torpedoWhereAnswer = (String) invocation.getArguments()[0];
			return PowerMockito.mock(OnGoingStringCondition.class);
		});
		final Query query = PowerMockito.mock(Query.class);
		PowerMockito.when(Torpedo.select(any(GitHubCredential.class))).thenReturn(query);
		PowerMockito.when(query.get(any())).thenReturn(Optional.empty());
		
		//Mocking Session class and their creation
		final SessionFactory sessionFactory = Mockito.mock(SessionFactory.class);
		this.session = Mockito.mock(Session.class);
		final Application app = Mockito.mock(Application.class);
		PowerMockito.mockStatic(Application.class);
		PowerMockito.when(Application.getInstance()).thenReturn(app);
		PowerMockito.when(Application.getInstance().getSessionFactory()).thenReturn(sessionFactory);
		PowerMockito.when(Application.getInstance().getSessionFactory().getCurrentSession()).thenReturn(this.session);
	}
	
	@Test(expected = IdentitySyntaxException.class)
	public void testNullParameter() {
		this.gitHubIdentityProvider.register(null);
	}
	
	@Test(expected = IdentitySyntaxException.class)
	public void testEmptyParameter() {
		this.gitHubIdentityProvider.register(new HashMap<>());
	}
	
	@Ignore
	@Test
	public void alreadyExistingAccessToken() throws Exception {
		//TODO
	}
	
	@Test
	public void gitHubCredentialCreation() throws Exception {
		//TODO
	}
	
	@Ignore
	@Test
	public void gitHubRequestTest() throws Exception {
		//TODO
	}
}
