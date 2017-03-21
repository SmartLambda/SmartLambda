package edu.teco.smartlambda.identity;

/**
 * Created on 08.03.17.
 */
public class GitHubCredentialDuplicateException extends IdentityException{
	public GitHubCredentialDuplicateException() {
		super();
	}
	
	public GitHubCredentialDuplicateException(final Throwable cause) {
		super(cause);
	}
	
	public GitHubCredentialDuplicateException(final String message) {
		super(message);
	}
}
