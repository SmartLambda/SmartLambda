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

/**
 * Provides REST calls related to lifecycle handling of access keys.
 */
public class KeyController {
	private KeyController() {
	}
	
	/**
	 * <code><b>PUT</b> /key/<i>:name</i></code>
	 * <p>
	 * Generates a new access key.
	 * </p>
	 * <p>
	 * The request must contain a new, unused key name. No body parameters are required. Responds with a JSON-encoded string that
	 * represents the generated key.
	 * </p>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is not permitted to create new keys
	 * @throws DuplicateKeyException            <b>409</b> Thrown in case a key with the specified name already exists
	 */
	public static Object createKey(final Request request, final Response response) {
		final User user = AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new);
		
		response.status(201);
		return user.createKey(request.params(":name")).getRight();
	}
	
	/**
	 * <code><b>DELETE</b> /key/<i>:name</i></code>
	 * <p>
	 * Deletes an existing access key.
	 * </p>
	 * <p>
	 * The request must contain the name of an existing key. No body parameters are required. Responds with an empty JSON object.
	 * </p>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the currently authenticated key is permitted to delete other keys
	 * @throws KeyNotFoundException             <b>404</b> Thrown when no key with the specified key does not exist
	 */
	public static Object deleteKey(final Request request, final Response response) {
		final User user = AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new);
		final Key  key  = user.getKeyByName(request.params(":name")).orElseThrow(() -> new KeyNotFoundException(request.params(":name")));
		key.delete();
		
		response.status(200);
		return new Object();
	}
}
