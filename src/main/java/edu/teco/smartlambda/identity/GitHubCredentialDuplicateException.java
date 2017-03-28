package edu.teco.smartlambda.identity;

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
