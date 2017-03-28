package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.teco.smartlambda.authentication.AuthenticationService;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.identity.GitHubIdentityProvider;
import edu.teco.smartlambda.identity.IdentityProvider;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
import edu.teco.smartlambda.identity.NullIdentityProvider;
import edu.teco.smartlambda.rest.exception.IdentityProviderNotFoundException;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides REST calls related to user account handling.
 */
public class UserController {
	@Data
	private static class RegistrationRequest {
		private String              identityProvider;
		private Map<String, String> parameters;
	}
	
	@Data
	private static class RegistrationResponse {
		private String name;
		private String primaryKey;
	}
	
	/**
	 * <code><b>GET</b> /users</code>
	 * <p>
	 * Lists users that are visible to the current user. No body parameters are required. Responds with a list of user name strings.
	 * </p>
	 *
	 * @throws NotAuthenticatedException <b>401</b> Thrown when user is not properly authenticated
	 */
	public static Object getUserList(final Request request, final Response response) {
		final User         user  = AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new);
		final List<String> users = new LinkedList<>();
		
		for (final User visible : user.getVisibleUsers())
			users.add(visible.getName());
		
		response.status(200);
		return users;
	}
	
	/**
	 * <code><b>POST</b> /register</code>
	 * <p>
	 *     Registers a new user.
	 * </p>
	 *
	 * <table>
	 *     <caption><b>Response values</b></caption>
	 *     <thead>
	 *         <tr>
	 *             <th>Name</th>
	 *             <th>Description</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>name</td>
	 *             <td>Name of the newly registered user</td>
	 *         </tr>
	 *         <tr>
	 *             <td>primaryKey</td>
	 *             <td>The primary key of the registered user, which may be used for future authentication</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 * <br><br>
	 * <table>
	 *     <caption><b>Request parameters</b></caption>
	 *     <thead>
	 *         <tr>
	 *             <th>Name</th>
	 *             <th>Type</th>
	 *             <th>Description</th>
	 *             <th>Required</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>identityProvider</td>
	 *             <td>enum("null", "github")</td>
	 *             <td>Identity provider to use for registration. See table below for further information.</td>
	 *             <td>Yes</td>
	 *         </tr>
	 *         <tr>
	 *             <td>parameters</td>
	 *             <td>object</td>
	 *             <td>Parameters to pass to the selected identity provider. See table below for further information.</td>
	 *             <td>Yes</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 * <br><br>
	 * <table>
	 *     <caption><b>Identity providers</b></caption>
	 *     <thead>
	 *         <tr>
	 *             <th>Name</th>
	 *             <th>Description</th>
	 *             <th>Required parameters</th>
	 *             <th>Implementation</th>
	 *         </tr>
	 *     </thead>
	 *     <tbody>
	 *         <tr>
	 *             <td>null</td>
	 *             <td>Simple identity provider that allows registration of any user name without external identification.</td>
	 *             <td>
	 *                 <table>
	 *                     <caption></caption>
	 *                     <tbody>
	 *                         <tr>
	 *                             <td>name</td>
	 *                             <td>User name</td>
	 *                         </tr>
	 *                     </tbody>
	 *                 </table>
	 *             </td>
	 *             <td>{@link NullIdentityProvider}</td>
	 *         </tr>
	 *         <tr>
	 *             <td>github</td>
	 *             <td>Identity provider that uses GitHub accounts for identification.</td>
	 *             <td>
	 *                 <table>
	 *                     <caption></caption>
	 *                     <tbody>
	 *                         <tr>
	 *                             <td>accessToken</td>
	 *                             <td>GitHub personal access token</td>
	 *                         </tr>
	 *                     </tbody>
	 *                 </table>
	 *             </td>
	 *             <td>{@link GitHubIdentityProvider}</td>
	 *         </tr>
	 *     </tbody>
	 * </table>
	 */
	public static Object register(final Request request, final Response response) throws IOException {
		final RegistrationRequest registrationRequest = new ObjectMapper().readValue(request.body(), RegistrationRequest.class);
		final IdentityProvider identityProvider =
				IdentityProviderRegistry.getInstance().getIdentityProviderByName(registrationRequest.getIdentityProvider()).orElseThrow(() -> new IdentityProviderNotFoundException(registrationRequest.getIdentityProvider()));
		final Pair<User, String>   userKey              = identityProvider.register(registrationRequest.getParameters());
		final RegistrationResponse registrationResponse = new RegistrationResponse();
		registrationResponse.setName(userKey.getLeft().getName());
		registrationResponse.setPrimaryKey(userKey.getRight());
		
		response.status(201);
		return registrationResponse;
	}
}
