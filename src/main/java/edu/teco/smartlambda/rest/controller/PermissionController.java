package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.teco.smartlambda.authentication.AuthenticationService;
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
		
		Arrays.stream(PermissionType.values()).filter(permissionType -> permissionType != PermissionType.GRANT || key.isPrimaryKey())
				.forEach(type -> permissions.put(type.name().toLowerCase(), new LinkedList<>()));
		
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
				final User permissionUser = User.getByName(lambdaPermissionOwnerAndName.getUser())
						.orElseThrow(() -> new UserNotFoundException(lambdaPermissionOwnerAndName.getUser()));
				
				if (lambdaPermissionOwnerAndName.getName() != null) key.grantPermission(LambdaFacade.getInstance().getFactory()
								.getLambdaByOwnerAndName(permissionUser, lambdaPermissionOwnerAndName.getName())
								.orElseThrow(() -> new LambdaNotFoundException(lambdaPermissionOwnerAndName.getName())),
						PermissionType.valueOf(type.toUpperCase()));
				else key.grantPermission(permissionUser, PermissionType.valueOf(type.toUpperCase()));
			}
		});
	}
	
	private static void revokePermissions(final Key key, final Map<String, List<LambdaPermissionOwnerAndName>> permissions) {
		permissions.forEach((type, list) -> {
			for (final LambdaPermissionOwnerAndName lambdaPermissionOwnerAndName : list) {
				final User permissionUser = User.getByName(lambdaPermissionOwnerAndName.getUser()).orElseThrow(() -> new UserNotFoundException(lambdaPermissionOwnerAndName.getUser()));
				
				if (lambdaPermissionOwnerAndName.getName() != null) key.revokePermission(
						LambdaFacade.getInstance().getFactory().getLambdaByOwnerAndName(permissionUser, lambdaPermissionOwnerAndName.getName())
								.orElseThrow(() -> new LambdaNotFoundException(lambdaPermissionOwnerAndName.getName())), PermissionType.valueOf(type.toUpperCase()));
				else key.revokePermission(permissionUser, PermissionType.valueOf(type.toUpperCase()));
			}
		});
	}
	
	private static User getUserFromRequest(final Request request) {
		return User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
	}
	
	private static Key getKeyFromRequest(final Request request) {
		return AuthenticationService.getInstance().getAuthenticatedUser().orElseThrow(NotAuthenticatedException::new)
				.getKeyByName(request.params(":name")).orElseThrow(() -> new KeyNotFoundException(request.params(":name")));
	}
	
	public static Object readUserPermissions(final Request request, final Response response) {
		return readPermissions(getUserFromRequest(request).getPrimaryKey());
	}
	
	public static Object grantUserPermissions(final Request request, final Response response) throws IOException {
		grantPermissions(getUserFromRequest(request).getPrimaryKey(), mapFromJSON(request.body()));
		
		return new Object();
	}
	
	public static Object revokeUserPermissions(final Request request, final Response response) throws IOException {
		revokePermissions(getUserFromRequest(request).getPrimaryKey(), mapFromJSON(request.body()));
		
		return new Object();
	}
	
	public static Object readKeyPermissions(final Request request, final Response response) {
		return readPermissions(getKeyFromRequest(request));
	}
	
	public static Object grantKeyPermissions(final Request request, final Response response) throws IOException {
		grantPermissions(getKeyFromRequest(request), mapFromJSON(request.body()));
		
		return new Object();
	}
	
	public static Object revokeKeyPermissions(final Request request, final Response response) throws IOException {
		revokePermissions(getKeyFromRequest(request), mapFromJSON(request.body()));
		
		return new Object();
	}
}
