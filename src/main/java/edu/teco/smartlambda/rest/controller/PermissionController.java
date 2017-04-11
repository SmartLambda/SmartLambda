package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.Permission;
import edu.teco.smartlambda.authentication.entities.PermissionType;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.LambdaFacade;
import edu.teco.smartlambda.rest.exception.KeyNotFoundException;
import edu.teco.smartlambda.rest.exception.LambdaNotFoundException;
import edu.teco.smartlambda.rest.exception.UserNotFoundException;
import lombok.Data;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides REST calls related to lifecycle handling of permissions.
 */
public class PermissionController {
	@Data
	private static class LambdaPermissionOwnerAndName {
		private String user;
		private String name;
	}
	
	private static Map<String, List<LambdaPermissionOwnerAndName>> mapFromJSON(final String json) throws IOException {
		final ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, objectMapper.getTypeFactory().constructFromCanonical(
				"java.util.HashMap<java.lang.String,java.util.LinkedList<edu.teco" +
						".smartlambda.rest.controller.PermissionController$LambdaPermissionOwnerAndName>>"));
	}
	
	private static Map<String, List<LambdaPermissionOwnerAndName>> readPermissions(final Key key) {
		final Map<String, List<LambdaPermissionOwnerAndName>> permissions = new HashMap<>();
		
		Arrays.stream(PermissionType.values()).filter(permissionType -> permissionType != PermissionType.GRANT || key.isPrimaryKey()).forEach(type -> permissions.put(type.name().toLowerCase(), new LinkedList<>()));
		
		for (final Permission permission : key.getVisiblePermissions()) {
			final LambdaPermissionOwnerAndName result = new LambdaPermissionOwnerAndName();
			result.setName(permission.getLambda() != null ? permission.getLambda().getName() : "*");
			result.setUser(permission.getUser() != null ? permission.getUser().getName() : permission.getLambda().getOwner().getName());
			
			permissions.get(permission.getPermissionType().name().toLowerCase()).add(result);
		}
		
		return permissions;
	}
	
	private static void grantPermissions(final Key key, final Map<String, List<LambdaPermissionOwnerAndName>> permissions) {
		permissions.forEach((type, list) -> {
			for (final LambdaPermissionOwnerAndName lambdaPermissionOwnerAndName : list) {
				final User permissionUser = User.getByName(lambdaPermissionOwnerAndName.getUser()).orElseThrow(() -> new UserNotFoundException(lambdaPermissionOwnerAndName.getUser()));
				
				if (!lambdaPermissionOwnerAndName.getName().equals("*")) key.grantPermission(LambdaFacade.getInstance().getFactory()
								.getLambdaByOwnerAndName(permissionUser, lambdaPermissionOwnerAndName.getName()).orElseThrow(() -> new LambdaNotFoundException(lambdaPermissionOwnerAndName.getName())),
						PermissionType.valueOf(type.toUpperCase()));
				else key.grantPermission(permissionUser, PermissionType.valueOf(type.toUpperCase()));
			}
		});
	}
	
	private static void revokePermissions(final Key key, final Map<String, List<LambdaPermissionOwnerAndName>> permissions) {
		permissions.forEach((type, list) -> {
			for (final LambdaPermissionOwnerAndName lambdaPermissionOwnerAndName : list) {
				final User permissionUser = User.getByName(lambdaPermissionOwnerAndName.getUser())
						.orElseThrow(() -> new UserNotFoundException(lambdaPermissionOwnerAndName.getUser()));
				
				if (!lambdaPermissionOwnerAndName.getName().equals("*")) key.revokePermission(LambdaFacade.getInstance().getFactory()
								.getLambdaByOwnerAndName(permissionUser, lambdaPermissionOwnerAndName.getName())
								.orElseThrow(() -> new LambdaNotFoundException(lambdaPermissionOwnerAndName.getName())),
						PermissionType.valueOf(type.toUpperCase()));
				else key.revokePermission(permissionUser, PermissionType.valueOf(type.toUpperCase()));
			}
		});
	}
	
	private static User getUserFromRequest(final Request request) {
		return User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
	}
	
	private static Key getKeyFromRequest(final Request request) {
		return AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new).getKeyByName(request.params(":name")).orElseThrow(() -> new KeyNotFoundException(request.params(":name")));
	}
	
	/**
	 * <code><b>GET</b> /<i>:user</i>/permissions</code>
	 * <p>
	 * Lists permissions granted to the specified user. No body parameters are required. The response contains an array of all
	 * granted permissions grouped by their types.
	 * <br>
	 * Permissions are either granted for a specific lambda or for all lambdas of a user.
	 * </p>
	 * <table>
	 * <caption><b>Permission types</b></caption>
	 * <thead>
	 * <tr>
	 * <th>Name</th>
	 * <th>Description</th>
	 * </tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>read</td>
	 * <td>Read lambda settings</td>
	 * </tr>
	 * <tr>
	 * <td>patch</td>
	 * <td>Change lambda settings (includes deploying a new binary or source code container)</td>
	 * </tr>
	 * <tr>
	 * <td>execute</td>
	 * <td>Execute lambdas</td>
	 * </tr>
	 * <tr>
	 * <td>delete</td>
	 * <td>Delete lambdas</td>
	 * </tr>
	 * <tr>
	 * <td>status</td>
	 * <td>Read lambda statistics</td>
	 * </tr>
	 * <tr>
	 * <td>schedule</td>
	 * <td>Create scheduled events</td>
	 * </tr>
	 * <tr>
	 * <td>create</td>
	 * <td>Create new lambdas</td>
	 * </tr>
	 * <tr>
	 * <td>grant</td>
	 * <td>Grant permissions to others
	 * <br>
	 * <b>Can't be granted to keys</b></td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * <br><br>
	 * <table>
	 * <caption><b>Permission object parameters</b></caption>
	 * <thead>
	 * <tr>
	 * <th>Name</th>
	 * <th>Type</th>
	 * <th>Description</th>
	 * </tr>
	 * </thead>
	 * <tbody>
	 * <tr>
	 * <td>user</td>
	 * <td>string</td>
	 * <td>User a permission is granted to</td>
	 * </tr>
	 * <tr>
	 * <td>lambda</td>
	 * <td>string</td>
	 * <td>Name of the lambda a permission is granted for, or '*' for permissions granted for all lambdas
	 * <br>
	 * <b>Option invalid for 'create' permissions</b>
	 * </td>
	 * </tr>
	 * </tbody>
	 * </table>
	 * <p>
	 * <br><br>
	 * <h3>Example</h3>
	 * <pre>
	 * <code>
	 *         {
	 *              "read":[],
	 *              "execute":[{"user":"foo","name":"*"}],
	 *              "grant":[{"user":"foo","name":"bar"}]
	 *         }
	 * </code>
	 * </pre>
	 *
	 * @throws NotAuthenticatedException <b>401</b> Thrown when user is not properly authenticated
	 * @throws UserNotFoundException     <b>404</b> Thrown when the specified user is unknown
	 */
	public static Object readUserPermissions(final Request request, final Response response) {
		final Object result = readPermissions(getUserFromRequest(request).getPrimaryKey());
		
		response.status(200);
		return result;
	}
	
	/**
	 * <code><b>PUT</b> /<i>:user</i>/permissions</code>
	 * <p>
	 * Grants permissions to the specified user. Body must contain a valid list of
	 * permissions (see {@link PermissionController#readUserPermissions} for an example). Responds with an empty JSON object on success.
	 * </p>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws UserNotFoundException            <b>404</b> Thrown when the specified user is unknown
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the authenticated user doesn't have sufficient permissions to grant
	 *                                          at least one of the specified permissions
	 * @see PermissionController#readUserPermissions
	 */
	public static Object grantUserPermissions(final Request request, final Response response) throws IOException {
		grantPermissions(getUserFromRequest(request).getPrimaryKey(), mapFromJSON(request.body()));
		
		response.status(200);
		return new Object();
	}
	
	/**
	 * <code><b>DELETE</b> /<i>:user</i>/permissions</code>
	 * <p>
	 * Revokes permissions previously granted to the specified user. Body must contain a valid list of
	 * permissions (see {@link PermissionController#readUserPermissions} for an example). Responds with an empty JSON object on success.
	 * </p>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws UserNotFoundException            <b>404</b> Thrown when the specified user is unknown
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the authenticated user doesn't have sufficient permissions to revoke
	 *                                          (grant) at least one of the specified permissions
	 * @see PermissionController#readUserPermissions
	 */
	public static Object revokeUserPermissions(final Request request, final Response response) throws IOException {
		revokePermissions(getUserFromRequest(request).getPrimaryKey(), mapFromJSON(request.body()));
		
		response.status(200);
		return new Object();
	}
	
	/**
	 * <code><b>GET</b> /key/<i>:name</i>/permissions</code>
	 * <p>
	 * Lists permissions granted to the specified user. No body parameters are required. See
	 * {@link PermissionController#readUserPermissions(Request, Response)} for a result example.
	 * </p>
	 *
	 * @throws NotAuthenticatedException <b>401</b> Thrown when user is not properly authenticated
	 * @throws UserNotFoundException     <b>404</b> Thrown when the specified user is unknown
	 * @see PermissionController#readUserPermissions
	 */
	public static Object readKeyPermissions(final Request request, final Response response) {
		final Object result = readPermissions(getKeyFromRequest(request));
		
		response.status(200);
		return result;
	}
	
	/**
	 * <code><b>PUT</b> /key/<i>:name</i>/permissions</code>
	 * <p>
	 * Grants permissions to the specified key. Body must contain a valid list of
	 * permissions (see {@link PermissionController#readUserPermissions} for an example). Responds with an empty JSON object on success.
	 * </p>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws UserNotFoundException            <b>404</b> Thrown when the specified user is unknown
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the authenticated user doesn't have sufficient permissions to grant
	 *                                          at least one of the specified permissions
	 * @see PermissionController#readUserPermissions
	 * @see PermissionController#grantUserPermissions
	 */
	public static Object grantKeyPermissions(final Request request, final Response response) throws IOException {
		grantPermissions(getKeyFromRequest(request), mapFromJSON(request.body()));
		
		response.status(200);
		return new Object();
	}
	
	/**
	 * <code><b>DELETE</b> /key/<i>:name</i>/permissions</code>
	 * <p>
	 * Revokes permissions previously granted to the specified key. Body must contain a valid list of
	 * permissions (see {@link PermissionController#readUserPermissions} for an example). Responds with an empty JSON object on success.
	 * </p>
	 *
	 * @throws NotAuthenticatedException        <b>401</b> Thrown when user is not properly authenticated
	 * @throws UserNotFoundException            <b>404</b> Thrown when the specified user is unknown
	 * @throws InsufficientPermissionsException <b>403</b> Thrown when the authenticated user doesn't have sufficient permissions to revoke
	 *                                          (grant) at least one of the specified permissions
	 * @see PermissionController#readUserPermissions
	 * @see PermissionController#revokeUserPermissions
	 */
	public static Object revokeKeyPermissions(final Request request, final Response response) throws IOException {
		revokePermissions(getKeyFromRequest(request), mapFromJSON(request.body()));
		
		response.status(200);
		return new Object();
	}
}
