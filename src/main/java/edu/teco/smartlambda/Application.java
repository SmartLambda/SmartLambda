package edu.teco.smartlambda;

import edu.teco.smartlambda.configuration.ConfigurationService;
import edu.teco.smartlambda.rest.controller.KeyController;
import edu.teco.smartlambda.rest.controller.LambdaController;
import edu.teco.smartlambda.rest.controller.PermissionController;
import edu.teco.smartlambda.rest.controller.ScheduleController;
import edu.teco.smartlambda.rest.controller.UserController;
import edu.teco.smartlambda.rest.filter.SessionEndFilter;
import edu.teco.smartlambda.rest.filter.SessionStartFilter;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import spark.Spark;

import java.io.File;

public class Application {
	private static Application instance = null;
	private        SessionFactory sessionFactory;
	
	private Application() {
		// Load runtimes
		RuntimeRegistry.getInstance();
		
		initializeHibernate();
		initializeSpark();
	}
	
	private void initializeSpark() {
		Spark.port(ConfigurationService.getInstance().getConfiguration().getInt("rest.port", 80));
		
		Spark.before(new SessionStartFilter());
		Spark.after(new SessionEndFilter());
		
		Spark.get("/:user/permissions", PermissionController::readUserPermissions);
		Spark.put("/:user/permissions", PermissionController::grantUserPermissions);
		Spark.delete("/:user/permissions", PermissionController::revokeUserPermissions);
		Spark.get("/:key/permissions", PermissionController::readKeyPermissions);
		Spark.put("/:key/permissions", PermissionController::grantKeyPermissions);
		Spark.delete("/:key/permissions", PermissionController::revokeKeyPermissions);
		
		Spark.put("/:user/lambda/:name", LambdaController::createLambda);
		Spark.patch("/:user/lambda/:name", LambdaController::updateLambda);
		Spark.get("/:user/lambda/:name", LambdaController::readLambda);
		Spark.delete("/:user/lambda/:name", LambdaController::deleteLambda);
		Spark.post("/:user/lambda/:name", LambdaController::executeLambda);
		Spark.get("/:user/lambdas", LambdaController::getLambdaList);
		Spark.get("/:user/lambda/:name/statistics", LambdaController::getStatistics);
		
		Spark.put("/:user/lambda/:name/schedule/:event-name", ScheduleController::createSchedule);
		Spark.patch("/:user/lambda/:name/schedule/:event-name", ScheduleController::updateSchedule);
		Spark.get("/:user/lambda/:name/schedule/:event-name", ScheduleController::readSchedule);
		Spark.delete("/:user/lambda/:name/schedule/:event-name", ScheduleController::deleteSchedule);
		Spark.get("/:user/lambda/:name/schedules", ScheduleController::getScheduledEvents);
		
		Spark.put("/key/:name", KeyController::createKey);
		Spark.delete("/key/:name", KeyController::deleteKey);
		
		Spark.get("/users", UserController::getUserList);
		Spark.put("/:user", UserController::register);
	}
	
	private void initializeHibernate() {
		final Configuration configuration = new Configuration();
		configuration.setProperty("hibernate.current_session_context_class", "thread");
		configuration.configure(new File(BuildConfig.HIBERNATE_CONFIGURATION_PATH));
		
		sessionFactory = configuration.buildSessionFactory();
	}
	
	/**
	 * @return a session factory initialized with the user-provided Hibernate configuration
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public static void main(final String... args) {
		getInstance();
	}
	
	/**
	 * @return the singleton Application instance
	 */
	public static Application getInstance() {
		if (instance == null) instance = new Application();
		
		return instance;
	}
}
