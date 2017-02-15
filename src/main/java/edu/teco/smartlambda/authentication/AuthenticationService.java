package edu.teco.smartlambda.authentication;

import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Optional;

/**
 * Created by Jonathan on 01.02.17.
 */
public class AuthenticationService {
	
	private static Configuration                      conf             = new Configuration();
	public static  SessionFactory                     sessionFactory   = conf.buildSessionFactory();
	private static ThreadLocal<AuthenticationService> instance         = null;
	private        Key                                authenticatedKey = null;
	
	public static AuthenticationService getInstance() {
		if (instance == null) {
			instance = new ThreadLocal<>();
			instance.set(new AuthenticationService());
		} else if (instance.get() == null) {
			instance.set(new AuthenticationService());
		}
		
		return instance.get();
	}
	
	private AuthenticationService() {
		
	}
	
	public void authenticate(final String key) {
		//TODO hash parameter key and search for it in the database. Then assign it to the local variable "authenticatedKey"
	}
	
	public void authenticate(final Key key) {
		authenticatedKey = key;
	}
	
	public Optional<Key> getAuthenticatedKey() {
		return new Optional.ofNullable(authenticatedKey);
	}
	
	public Optional<User> getAuthenticatedUser() {
		if (authenticatedKey != null) {
			return new Optional.of(authenticatedKey.getUser());
		}
		return Optional.empty();
	}
}
