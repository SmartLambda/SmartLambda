package edu.teco.smartlambda.rest.filter;

import edu.teco.smartlambda.Application;
import spark.Filter;
import spark.Request;
import spark.Response;

public class SessionStartFilter implements Filter {
	@Override
	public void handle(final Request request, final Response response) throws Exception {
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction().getStatus();
	}
}
