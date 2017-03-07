package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * Created on 28.02.17.
 */
public class GitHubIdentityProvider implements IdentityProvider{
	private static final String NAME = "github";
	
	@Override
	public Pair<User, String> register(final Map<String, String> parameters) throws IdentityException {
		final String accessToken = parameters.get("accessToken");
		if (accessToken == null) {
			throw new IdentitySyntaxException();
		}
		final String name = "";
		//TODO ask GitHub for the name, throw Exception otherwise on error throw InvalidCredentialsException
		
		final GitHubCredential credential = new GitHubCredential(accessToken);
		Application.getInstance().getSessionFactory().getCurrentSession().save(credential);
				
		return User.createUser(name);
	}
	
	@Override
	public String getName() {
		return NAME;
	}
}
