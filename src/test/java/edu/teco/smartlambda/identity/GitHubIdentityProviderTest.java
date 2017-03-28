package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
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

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created on 22.03.17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({User.class, Torpedo.class, Application.class, URL.class, GitHubIdentityProvider.class})
public class GitHubIdentityProviderTest {
	
	private String                 createUserAnswer;
	private User                   user;
	private Class                  torpedoFromAnswer;
	private String                 torpedoWhereAnswer;
	private Object                 sessionSaveAnswer;
	private Session                session;
	private OnGoingStringCondition onGoingStringCondition;
	private GitHubCredential       gitHubCredential;
	private HttpURLConnection      connection;
	private URL                    url;
	
	private final String                 gitHubHttpQueryUsernameResponse = "USERNAME";
	
	private GitHubIdentityProvider gitHubIdentityProvider;
	
	@Before
	public void setUp() throws Exception {
		this.gitHubIdentityProvider = new GitHubIdentityProvider();
		
		//Don't create a new User but check the supplied name
		PowerMockito.mockStatic(User.class);
		this.user = PowerMockito.mock(User.class);
		PowerMockito.when(User.createUser(anyString())).thenAnswer(invocation -> {
			this.createUserAnswer = (String) invocation.getArguments()[0];
			return Pair.of(this.user, "");
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
		PowerMockito.when(this.session.save(any(Object.class))).thenAnswer(invocation -> {
			this.sessionSaveAnswer = invocation.getArguments()[0];
			return null;
		});
		
		//Mock HTTP connection classes
		this.connection = PowerMockito.mock(HttpURLConnection.class);
		PowerMockito.mockStatic(URL.class);
		this.url = PowerMockito.mock(URL.class);
		PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(this.url);
		Mockito.when(this.url.openConnection()).thenReturn(this.connection);
		PowerMockito.when(this.connection.getResponseCode()).thenReturn(200);
		PowerMockito.when(this.connection.getInputStream()).thenReturn(new ByteArrayInputStream("{\"login\":\"".concat
				(this.gitHubHttpQueryUsernameResponse + "\"}").getBytes() ));
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
	public void testTorpedoqueryQuery() throws Exception {
		//TODO
	}
	
	@Test
	public void gitHubCredentialCreation() throws Exception {
		final String token = "GitHubIdentityProviderTest.gitHubCredentialCreation.Token";
		
		final Map<String, String> params = new HashMap<>();
		params.put("accessToken", token);
		this.gitHubIdentityProvider.register(params);
		
		final GitHubCredential savedCredential = (GitHubCredential) this.sessionSaveAnswer;
		Assert.assertEquals(token, savedCredential.getAccessToken());
		Assert.assertEquals(this.user, savedCredential.getUser());
	}
	
	@Test (expected = InvalidCredentialsException.class)
	public void gitHubAuthenticationBadResponseCode() throws Exception {
		final String token = "GitHubIdentityProviderTest.gitHubAuthenticationBadResponseCode.Token";
		PowerMockito.when(this.connection.getResponseCode()).thenReturn(9999);
		PowerMockito.when(this.connection.getErrorStream()).thenReturn(new ByteArrayInputStream( "mocked InputStream".getBytes() ));
		
		final Map<String, String> params = new HashMap<>();
		params.put("accessToken", token);
		this.gitHubIdentityProvider.register(params);
	}
	
	@Test
	public void gitHubAuthenticationValidResponseCode() throws Exception {
		final String token = "GitHubIdentityProviderTest.gitHubAuthenticationValidResponseCode.Token";
		
		final Map<String, String> params = new HashMap<>();
		params.put("accessToken", token);
		this.gitHubIdentityProvider.register(params);
		Assert.assertEquals(this.createUserAnswer, this.gitHubHttpQueryUsernameResponse);
	}
}
