package edu.teco.smartlambda.utility;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
	
	/**
	 * Test all getters and setters of an object instance. The instance should not be used anymore afterwards, since it calls every
	 * setter with 'null' which may lead to an unstable state. Furthermore, this method shall not be used, if methods exist in a class
	 * which are called like simple getters and setters (set***(...), get***()), that are no simple getters and setters.
	 *
	 * @param o an object instance of the class whose methods shall be tested
	 *
	 * @throws InvocationTargetException If any exception rises in the getters/setters
	 * @throws IllegalAccessException    this exception is highly unexpected, because the constructor is made accessible by reflection
	 */
	public static void testGettersAndSetters(final Object o) throws InvocationTargetException, IllegalAccessException {
		for (final Method method : o.getClass().getDeclaredMethods()) {
			method.setAccessible(true);
			if (method.getName().startsWith("get") && method.getParameterCount() == 0 && !method.getReturnType().equals(Void.TYPE)) {
				assert method.invoke(o) != null;
			} else if (method.getName().startsWith("set") && method.getParameterCount() == 1 && method.getReturnType().equals(Void.TYPE)) {
				method.invoke(o, new Object[] {null});
			}
		}
	}
}
