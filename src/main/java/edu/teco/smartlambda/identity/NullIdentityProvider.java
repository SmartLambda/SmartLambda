package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

/**
 * Created on 28.02.17.
 */
public class NullIdentityProvider implements IdentityProvider{
	private static final String NAME = "null";
	
	@Override
	public Pair<User, String> register(final Map<String, String> parameters) throws IdentityException {
		String name = parameters.get("name");
		if (name == null) {
			throw new IdentitySyntaxException();
		}
		//User user = User.createUser(NAME).getLeft();
		//Application.getInstance().getSessionFactory().getCurrentSession().save(user);
		//return user;
		return User.createUser(name);
	}
	
	@Override
	public String getName() {
		return NAME;
	}
}
