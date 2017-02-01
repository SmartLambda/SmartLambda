package edu.teco.smartlambda.authentication;

/**
 * Created by Jonathan on 01.02.17.
 */
public class AuthenticationService {
	
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
	

}
