package edu.teco.smartlambda;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.google.gson.Gson;
import edu.teco.smartlambda.authentication.DuplicateKeyException;
import edu.teco.smartlambda.authentication.DuplicateUserException;
import edu.teco.smartlambda.authentication.InsufficientPermissionsException;
import edu.teco.smartlambda.authentication.NotAuthenticatedException;
import edu.teco.smartlambda.authentication.entities.Key;
import edu.teco.smartlambda.authentication.entities.Permission;
import edu.teco.smartlambda.authentication.entities.User;
import edu.teco.smartlambda.concurrent.ThreadManager;
import edu.teco.smartlambda.configuration.ConfigurationService;
import edu.teco.smartlambda.identity.GitHubCredential;
import edu.teco.smartlambda.identity.GitHubCredentialDuplicateException;
import edu.teco.smartlambda.identity.IdentityException;
import edu.teco.smartlambda.identity.IdentityProviderRegistry;
import edu.teco.smartlambda.lambda.DuplicateEventException;
import edu.teco.smartlambda.lambda.DuplicateLambdaException;
import edu.teco.smartlambda.lambda.InvalidLambdaException;
import edu.teco.smartlambda.lambda.Lambda;
import edu.teco.smartlambda.monitoring.MonitoringEvent;
import edu.teco.smartlambda.rest.controller.KeyController;
import edu.teco.smartlambda.rest.controller.LambdaController;
import edu.teco.smartlambda.rest.controller.PermissionController;
import edu.teco.smartlambda.rest.controller.ScheduleController;
import edu.teco.smartlambda.rest.controller.UserController;
import edu.teco.smartlambda.rest.exception.EventNotFoundException;
import edu.teco.smartlambda.rest.exception.IdentityProviderNotFoundException;
import edu.teco.smartlambda.rest.exception.InvalidLambdaDefinitionException;
import edu.teco.smartlambda.rest.exception.LambdaNotFoundException;
import edu.teco.smartlambda.rest.exception.UserNotFoundException;
import edu.teco.smartlambda.rest.filter.AccessControlFilter;
import edu.teco.smartlambda.rest.filter.AuthenticationFilter;
import edu.teco.smartlambda.rest.filter.SessionEndFilter;
import edu.teco.smartlambda.rest.filter.SessionStartFilter;
import edu.teco.smartlambda.rest.response.ExceptionResponse;
import edu.teco.smartlambda.runtime.RuntimeRegistry;
import edu.teco.smartlambda.schedule.CronExpressionException;
import edu.teco.smartlambda.schedule.Event;
import edu.teco.smartlambda.schedule.ScheduleManager;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.io.File;
import java.util.concurrent.Future;

public class Application {
	private static Future<Void> scheduleManagerFuture;
	private static Application instance = null;
	private SessionFactory sessionFactory;
	
	private Application() {
		this.initializeHibernate();
	}
	
	private void start() {
		RuntimeRegistry.getInstance();
		IdentityProviderRegistry.getInstance();
		this.initializeSpark();
		scheduleManagerFuture = ThreadManager.getExecutorService().submit(ScheduleManager.getInstance()::run);
	}
	
	private void initializeSpark() {
		final Gson gson = new Gson();
		
		Spark.port(ConfigurationService.getInstance().getConfiguration().getInt("rest.port", 80));
		
		Spark.before(new SessionStartFilter());
		Spark.before(new AuthenticationFilter());
		Spark.before(new AccessControlFilter());
		Spark.after(new SessionEndFilter());
		
		Spark.options("/*", (request, response) -> {
			response.header("Access-Control-Allow-Headers", "SmartLambda-Key, Content-Type");
			return new Object();
		}, gson::toJson);
		
		Spark.get("/:user/permissions", PermissionController::readUserPermissions, gson::toJson);
		Spark.put("/:user/permissions", PermissionController::grantUserPermissions, gson::toJson);
		Spark.delete("/:user/permissions", PermissionController::revokeUserPermissions, gson::toJson);
		Spark.get("/key/:name/permissions", PermissionController::readKeyPermissions, gson::toJson);
		Spark.put("/key/:name/permissions", PermissionController::grantKeyPermissions, gson::toJson);
		Spark.delete("/key/:name/permissions", PermissionController::revokeKeyPermissions, gson::toJson);
		
		Spark.put("/:user/lambda/:name", LambdaController::createLambda, gson::toJson);
		Spark.patch("/:user/lambda/:name", LambdaController::updateLambda, gson::toJson);
		Spark.get("/:user/lambda/:name", LambdaController::readLambda, gson::toJson);
		Spark.delete("/:user/lambda/:name", LambdaController::deleteLambda, gson::toJson);
		Spark.post("/:user/lambda/:name", LambdaController::executeLambda);
		Spark.get("/:user/lambdas", LambdaController::getLambdaList, gson::toJson);
		Spark.get("/:user/lambda/:name/statistics", LambdaController::getStatistics, gson::toJson);
		
		Spark.put("/:user/lambda/:name/schedule/:event-name", ScheduleController::createSchedule, gson::toJson);
		Spark.patch("/:user/lambda/:name/schedule/:event-name", ScheduleController::updateSchedule, gson::toJson);
		Spark.get("/:user/lambda/:name/schedule/:event-name", ScheduleController::readSchedule, gson::toJson);
		Spark.delete("/:user/lambda/:name/schedule/:event-name", ScheduleController::deleteSchedule, gson::toJson);
		Spark.get("/:user/lambda/:name/schedules", ScheduleController::getScheduledEvents, gson::toJson);
		
		Spark.put("/key/:name", KeyController::createKey, gson::toJson);
		Spark.delete("/key/:name", KeyController::deleteKey, gson::toJson);
		
		Spark.get("/users", UserController::getUserList, gson::toJson);
		Spark.post("/register", UserController::register, gson::toJson);
		
		Spark.exception(Exception.class, (Exception exception, Request request, Response response) -> {
			response.status(500);
			response.body("");
			exception.printStackTrace();
		});
		
		Spark.exception(CronExpressionException.class, (Exception exception, Request request, Response response) -> {
			response.status(400);
			response.body(gson.toJson(new ExceptionResponse("Invalid Cron-Expression: " + exception.getMessage())));
		});
		
		Spark.exception(JsonMappingException.class, (Exception exception, Request request, Response response) -> {
			response.status(400);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(InvalidFormatException.class, (Exception exception, Request request, Response response) -> {
			response.status(400);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(UnrecognizedPropertyException.class, (Exception exception, Request request, Response response) -> {
			response.status(400);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(InvalidLambdaDefinitionException.class, (Exception exception, Request request, Response response) -> {
			response.status(400);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(UserNotFoundException.class, (Exception exception, Request request, Response response) -> {
			response.status(404);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(LambdaNotFoundException.class, (Exception exception, Request request, Response response) -> {
			response.status(404);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(IdentityProviderNotFoundException.class, (Exception exception, Request request, Response response) -> {
			response.status(404);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(IdentityException.class, (Exception exception, Request request, Response response) -> {
			response.status(400);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(EventNotFoundException.class, (Exception exception, Request request, Response response) -> {
			response.status(404);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(NotAuthenticatedException.class, (Exception exception, Request request, Response response) -> {
			response.status(401);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(DuplicateUserException.class, (Exception exception, Request request, Response response) -> {
			response.status(409);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(DuplicateKeyException.class, (Exception exception, Request request, Response response) -> {
			response.status(409);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(DuplicateLambdaException.class, (Exception exception, Request request, Response response) -> {
			response.status(409);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(DuplicateEventException.class, (Exception exception, Request request, Response response) -> {
			response.status(409);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(GitHubCredentialDuplicateException.class, (Exception exception, Request request, Response response) -> {
			response.status(409);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(InsufficientPermissionsException.class, (Exception exception, Request request, Response response) -> {
			response.status(403);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
		
		Spark.exception(InvalidLambdaException.class, (Exception exception, Request request, Response response) -> {
			response.status(400);
			response.body(gson.toJson(new ExceptionResponse(exception.getMessage())));
		});
	}
	
	private void initializeHibernate() {
		final Configuration configuration = new Configuration();
		configuration.setProperty("hibernate.current_session_context_class", "thread");
		configuration.setProperty("hibernate.globally_quoted_identifiers", "true");
		configuration.addAnnotatedClass(Key.class);
		configuration.addAnnotatedClass(User.class);
		configuration.addAnnotatedClass(Permission.class);
		configuration.addAnnotatedClass(Lambda.class);
		configuration.addAnnotatedClass(MonitoringEvent.class);
		configuration.addAnnotatedClass(Event.class);
		configuration.addAnnotatedClass(GitHubCredential.class);
		configuration.configure(new File(BuildConfig.HIBERNATE_CONFIGURATION_PATH));
		
		this.sessionFactory = configuration.buildSessionFactory();
	}
	
	/**
	 * @return a session factory initialized with the user-provided Hibernate configuration
	 */
	public SessionFactory getSessionFactory() {
		return this.sessionFactory;
	}
	
	/**
	 * Application main entry point
	 *
	 * @param args ignored terminal arguments
	 */
	public static void main(final String... args) {
		getInstance().start();
		
		java.lang.Runtime.getRuntime().addShutdownHook(new Thread(Application::shutdown));
	}
	
	/**
	 * Called upon shutdown of the application
	 */
	private static void shutdown() {
		Spark.stop();
		ScheduleManager.getInstance().setRunning(false);
		getInstance().getSessionFactory().close();
	}
	
	/**
	 * @return the singleton Application instance
	 */
	public static Application getInstance() {
		if (instance == null) instance = new Application();
		
		return instance;
	}
}
