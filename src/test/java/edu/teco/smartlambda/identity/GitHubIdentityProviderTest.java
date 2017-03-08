package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;
/**
 * Created on 07.03.17.
 */
public class GitHubIdentityProviderTest {
	final String GITHUB_USERNAME = "umacs4hj2z";
	final String GITHUB_TOKEN = "209318dc0479214916b0b56c4d7b69755f8f8465";
	String token;
	User user;
	@Before
	public void buildUp() {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		final Map<String, String> params = new HashMap<>();
		//This is a personal GitHub authentication token from an existing GitHub-account. It's name is verified in gitHubAuthentication()
		this.token = GITHUB_TOKEN;
		params.put("accessToken", this.token);
		this.user = new GitHubIdentityProvider().register(params).getLeft();
	}
	
	@After
	public void tearDown() throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		if (transaction.isActive()) transaction.rollback();
	}
	
	@Test
	public void gitHubCredentialPersistence() {
		final GitHubCredential query = from(GitHubCredential.class);
		where(query.getAccessToken()).eq(this.token);
		Assert.assertFalse(select(query).list(Application.getInstance().getSessionFactory().getCurrentSession()).isEmpty());
	}
	
	@Test
	public void gitHubAuthentication() {
		Assert.assertTrue(this.user.getName().equals(GITHUB_USERNAME));
	}
}
