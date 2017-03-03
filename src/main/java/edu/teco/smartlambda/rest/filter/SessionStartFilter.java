package edu.teco.smartlambda.rest.filter;

import edu.teco.smartlambda.Application;
import org.hibernate.Transaction;
import spark.Filter;
import spark.Request;
import spark.Response;

public class SessionStartFilter implements Filter {
	@Override
	public void handle(final Request request, final Response response) throws Exception {
		final Transaction transaction = Application.getInstance().getSessionFactory().getCurrentSession().getTransaction();
		
		if (transaction.isActive()) transaction.rollback();
		
		Application.getInstance().getSessionFactory().getCurrentSession().beginTransaction();
	}
}
