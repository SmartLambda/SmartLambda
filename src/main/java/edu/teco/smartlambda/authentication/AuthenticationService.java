package edu.teco.smartlambda.authentication;
import edu.teco.smartlambda.authentication.entities.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.Optional;

/**
 * Created by Jonathan on 01.02.17.
 */
public class AuthenticationService {
	private static Configuration conf = new Configuration();
	public static SessionFactory sessionFactory = conf.buildSessionFactory();
	private static ThreadLocal<AuthenticationService> ourInstance = null;
	private Key authenticatedKey = null;
	
	public static AuthenticationService getInstance() {
		if (ourInstance == null) {
			AuthenticationService as = new AuthenticationService();
			ourInstance = new ThreadLocal<AuthenticationService>();
			ourInstance.set(as);
		}
		return ourInstance.get();
	}
	
	private AuthenticationService() {
		
	}
	
	public void authenticate(String key) {
		//TODO hash parameter key and search for it in the database. Then assign it to the local variable "authenticatedKey"
	}
	
	public void authenticate(Key key) {
		authenticatedKey = key;
	}

	public Optional<Key> getAuthenticatedKey() {
		return new Optional<Key>.ofNullable(authenticatedKey);
	}
	
	public Optional<User> getAuthenticatedUser() {
		if (authenticatedKey != null) {
			return new Optional<Key>.ofNullable(authenticatedKey.getUser());
		}
		return Optional.empty();
	}
	
	
}
