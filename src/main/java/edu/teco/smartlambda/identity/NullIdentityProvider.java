package edu.teco.smartlambda.identity;

import edu.teco.smartlambda.authentication.entities.User;

import java.util.Map;

/**
 * Created on 28.02.17.
 */
public class NullIdentityProvider implements IdentityProvider{
	private String name = NullIdentityProvider.class.getName();
	
	NullIdentityProvider() {
		
	}
	
	@Override
	public User register(final Map<String, String> parameters) throws IdentityException {
		name = parameters.get("name");
		if (name == null) {
			throw new IdentitySyntaxException();
		}
		//User user = User.createUser(name).getLeft();
		//Application.getInstance().getSessionFactory().getCurrentSession().save(user);
		//return user;
		return User.createUser(name).getLeft();
	}
	
	@Override
	public String getName() {
		return name;
	}
}
