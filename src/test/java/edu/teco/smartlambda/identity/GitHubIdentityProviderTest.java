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
	String token;
	@Before
	public void buildUp() {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
		final Map<String, String> params = new HashMap<>();
		this.token ="randomTestAccessToken";
		params.put("accessToken", this.token);
		final User user = new GitHubIdentityProvider().register(params).getLeft();
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
}
