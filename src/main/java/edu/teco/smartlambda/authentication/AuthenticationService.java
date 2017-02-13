package edu.teco.smartlambda.authentication;
import edu.teco.smartlambda.authentication.entities.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.Optional;

/**
 * Created by Jonathan on 01.02.17.
 */
public class AuthenticationService {
	Configuration conf = new Configuration();
	SessionFactory sessionFactory = conf.buildSessionFactory();
	private static ThreadLocal<AuthenticationService> ourInstance = null;
	//private Key authenticatedKey;
	
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
		
	}
	
	public void authenticate(Key key) {
		
	}

	public Optional<Key> getAuthenticatedKey() {
		return Optional.empty();
	}
	
	public Optional<User> getAuthenticatedUser() {
		return Optional.empty();
	}
	
	
}
