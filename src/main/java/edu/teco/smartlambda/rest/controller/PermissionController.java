package edu.teco.smartlambda.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.teco.smartlambda.authentication.entities.Permission;
import edu.teco.smartlambda.authentication.entities.PermissionType;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.lambda.LambdaFacade;
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
	
	public static Object readUserPermissions(final Request request, final Response response) {
		final User                                            user        =
				User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		final Map<String, List<LambdaPermissionOwnerAndName>> permissions = new HashMap<>();
		
		Arrays.stream(PermissionType.values()).forEach(type -> permissions.put(type.name().toLowerCase(), new LinkedList<>()));
		
		for (final Permission permission : user.getPrimaryKey().getVisiblePermissions()) {
			final LambdaPermissionOwnerAndName result = new LambdaPermissionOwnerAndName();
			result.setName(permission.getLambda() != null ? permission.getLambda().getName() : "*");
			result.setUser(permission.getUser() != null ? permission.getUser().getName() : permission.getLambda().getOwner().getName());
			
			permissions.get(permission.getPermissionType().name().toLowerCase()).add(result);
		}
		
		return permissions;
	}
	
	private static Map<String, List<LambdaPermissionOwnerAndName>> mapFromJSON(final String json) throws IOException {
		final ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(json, objectMapper.getTypeFactory().constructFromCanonical(
				"java.util.HashMap<java.lang.String,java.util.LinkedList<edu.teco" +
						".smartlambda.rest.controller.PermissionController$LambdaPermissionOwnerAndName>>"));
	}
	
	public static Object grantUserPermissions(final Request request, final Response response) throws IOException {
		final User user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		
		mapFromJSON(request.body()).forEach((type, list) -> {
			for (final LambdaPermissionOwnerAndName lambdaPermissionOwnerAndName : list) {
				final User permissionUser = User.getByName(lambdaPermissionOwnerAndName.getUser())
						.orElseThrow(() -> new UserNotFoundException(lambdaPermissionOwnerAndName.getUser()));
				
				if (lambdaPermissionOwnerAndName.getName() != null) user.getPrimaryKey().grantPermission(
						LambdaFacade.getInstance().getFactory()
								.getLambdaByOwnerAndName(permissionUser, lambdaPermissionOwnerAndName.getName())
								.orElseThrow(() -> new LambdaNotFoundException(lambdaPermissionOwnerAndName.getName())),
						PermissionType.valueOf(type.toUpperCase()));
				else user.getPrimaryKey().grantPermission(permissionUser, PermissionType.valueOf(type.toUpperCase()));
			}
		});
		
		return new Object();
	}
	
	public static Object revokeUserPermissions(final Request request, final Response response) throws IOException {
		final User user = User.getByName(request.params(":user")).orElseThrow(() -> new UserNotFoundException(request.params(":user")));
		
		mapFromJSON(request.body()).forEach((type, list) -> {
			for (final LambdaPermissionOwnerAndName lambdaPermissionOwnerAndName : list) {
				final User permissionUser = User.getByName(lambdaPermissionOwnerAndName.getUser())
						.orElseThrow(() -> new UserNotFoundException(lambdaPermissionOwnerAndName.getUser()));
				
				if (lambdaPermissionOwnerAndName.getName() != null) user.getPrimaryKey().revokePermission(
						LambdaFacade.getInstance().getFactory()
								.getLambdaByOwnerAndName(permissionUser, lambdaPermissionOwnerAndName.getName())
								.orElseThrow(() -> new LambdaNotFoundException(lambdaPermissionOwnerAndName.getName())),
						PermissionType.valueOf(type.toUpperCase()));
				else user.getPrimaryKey().revokePermission(permissionUser, PermissionType.valueOf(type.toUpperCase()));
			}
		});
		
		return new Object();
	}
	
	public static Object readKeyPermissions(final Request request, final Response response) {
		return null;
	}
	
	public static Object grantKeyPermissions(final Request request, final Response response) {
		return null;
	}
	
	public static Object revokeKeyPermissions(final Request request, final Response response) {
		return null;
	}
}
