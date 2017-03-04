package edu.teco.smartlambda.rest.filter;

import edu.teco.smartlambda.authentication.AuthenticationService;
import spark.Filter;
import spark.Request;
import spark.Response;

public class AuthenticationFilter implements Filter {
	@Override
	public void handle(final Request request, final Response response) throws Exception {
		final String key = request.headers("SmartLambda-Key");
		if (key != null) AuthenticationService.getInstance().authenticate(key);
	}
}
