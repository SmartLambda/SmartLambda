package edu.teco.smartlambda.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A utility class for test utilities.
 */
public final class TestUtility {
	
	/**
	 * This class shall not be instantiated
	 */
	private TestUtility() {
		
	}
	
	/**
	 * Invoke a private default constructor of a class. This method is mainly used to reach full coverage for classes, that shall not be
	 * instantiated
	 *
	 * @param clazz the class whose constructor shall be called
	 *
	 * @throws IllegalAccessException    this exception is highly unexpected, because the constructor is made accessible by reflection
	 * @throws InvocationTargetException if an exception is thrown by the called constructor
	 * @throws InstantiationException    highly unexpected, because the {@link Class#newInstance()} method isn't used
	 */
	public static void coverPrivateDefaultConstructor(final Class<?> clazz)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if (constructor.getParameterCount() > 0 || constructor.isAccessible()) continue;
			constructor.setAccessible(true);
			constructor.newInstance();
		}
	}
	
	/**
	 * Cover all constructors of an exception
	 *
	 * @param clazz the exception class to cover
	 *
	 * @throws IllegalAccessException    this exception is highly unexpected, because the constructor is made accessible by reflection
	 * @throws InvocationTargetException if an exception is thrown by the called constructor
	 * @throws InstantiationException    highly unexpected, because the {@link Class#newInstance()} method isn't used
	 */
	public static void coverException(final Class<? extends Throwable> clazz)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			constructor.setAccessible(true);
			constructor.newInstance(new Object[constructor.getParameterCount()]);
		}
	}
}
