package edu.teco.smartlambda.concurrent;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;

/**
 * A Utility class providing a {@link ListeningExecutorService}
 */
public final class ThreadManager {
	
	private final static ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
	
	/**
	 * Private default constructor. This class shall not be instanced
	 */
	private ThreadManager() {
		// intentionally empty
	}
	
	/**
	 * @return a global listening executor service as thread pool
	 */
	public static ListeningExecutorService getExecutorService() {
		return executorService;
	}
}
