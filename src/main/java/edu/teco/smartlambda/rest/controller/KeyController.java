package edu.teco.smartlambda.rest.controller;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NameConflictException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.User;
import spark.Request;
import spark.Response;

public class KeyController {
	public static Object createKey(final Request request, final Response response)
			throws InsufficientPermissionsException, NameConflictException {
		final User user = AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new);
		return user.createKey(request.params(":name")).getRight();
	}
	
	public static Object deleteKey(final Request request, final Response response) {
		return null;
	}
}
