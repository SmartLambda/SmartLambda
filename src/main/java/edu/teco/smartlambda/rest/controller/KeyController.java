package edu.teco.smartlambda.rest.controller;

import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.DuplicateKeyException;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.rest.exception.KeyNotFoundException;
import spark.Request;
import spark.Response;

public class KeyController {
	public static Object createKey(final Request request, final Response response)
			throws InsufficientPermissionsException, DuplicateKeyException {
		final User user = AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new);
		
		response.status(201);
		return user.createKey(request.params(":name")).getRight();
	}
	
	public static Object deleteKey(final Request request, final Response response) {
		final User user = AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new);
		final Key  key  = user.getKeyByName(request.params(":name")).orElseThrow(() -> new KeyNotFoundException(request.params(":name")));
		key.delete();
		
		response.status(200);
		return new Object();
	}
}
