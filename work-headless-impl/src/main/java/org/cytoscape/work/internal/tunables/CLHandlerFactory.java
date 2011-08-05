package org.cytoscape.work.internal.tunables;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.BoundedLong;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;

/**
 * Factory that creates commandline <code>Handlers</code> for Objects defined in Field or by Methods, depending on their types
 * 
 * @author pasteur
 *
 */
public class CLHandlerFactory implements TunableHandlerFactory<CLHandler> {

	public CLHandler getHandler(final Method m, final Object o, final Tunable t) {
		return null;
	}


	/**
	 * To get a <code>CLHandler</code> for Methods
	 * 
	 * @param getter Method that returns the value from the Object <code>o</code>
	 * @param setter Method that sets a value to the Object <code>o</code>
	 * @param o Object whose value will be set and get by the methods
	 * @param tg <code>Tunable</code> annotations of the Method <code>getter</code> annotated as <code>Tunable</code>
	 * @param ts <code>Tunable</code> annotations of the Method <code>setter</code> annotated as <code>Tunable</code>
	 * 
	 * @return a specific <code>CLHandler</code> of a type
	 */
	public CLHandler getHandler(final Method getter, final Method setter, final Object o, final Tunable tunable) {
		Class<?>[] paramsTypes = setter.getParameterTypes();
		Class<?> returnType = getter.getReturnType();
		Class<?> type = paramsTypes[0];
		if (type != returnType) {
			System.err.println("return type and parameter type are differents for the methods " + getter.getName() + " and " + setter.getName());
			return null;
		}
		
		if (type == Boolean.class || type == boolean.class)
			return new BooleanCLHandler(getter, setter, o, tunable);
		else if (type == String.class)
			return new StringCLHandler(getter, setter, o, tunable);
		else if (type == int.class || type == Integer.class)
			return new IntCLHandler(getter, setter, o, tunable);
		else if (type == float.class || type == Float.class)
			return new FloatCLHandler(getter, setter, o, tunable);
		else if (type == long.class || type == Long.class)
			return new LongCLHandler(getter, setter, o, tunable);
		else if (type == double.class || type == Double.class)
			return new DoubleCLHandler(getter, setter, o, tunable);
		else if (type == BoundedInteger.class)
			return new BoundedCLHandler<BoundedInteger>(getter, setter, o, tunable);
		else if (type == BoundedDouble.class)
			return new BoundedCLHandler<BoundedDouble>(getter, setter, o, tunable);
		else if (type == BoundedLong.class)
			return new BoundedCLHandler<BoundedLong>(getter, setter, o, tunable);
		else if (type == BoundedFloat.class)
			return new BoundedCLHandler<BoundedFloat>(getter, setter, o, tunable);
		else if (type == ListSingleSelection.class)
			return new ListSingleSelectionCLHandler<Object>(getter, setter, o, tunable);
		else if (type == ListMultipleSelection.class)
			return new ListMultipleSelectionCLHandler<Object>(getter, setter, o, tunable);
		else if (type == File.class)
			return new FileCLHandler(getter, setter, o, tunable);
		else if (type == URL.class)
			return new URLCLHandler(getter, setter, o, tunable);
		else if (type == InputStream.class)
			return new InputStreamCLHandler(getter, setter, o, tunable);
		else
			return null;
	}
	
	/**
	 * To get a <code>CLHandler</code> for a Field
	 * 
	 * @param f Field that is intercepted
	 * @param o Object that is contained in the Field <code>f</code>
	 * @param t <code>Tunable</code> annotations of the Field <code>f</code> annotated as <code>Tunable</code>
	 * 
	 * @return a specific <code>CLHandler</code> of a type
	 */
	public CLHandler getHandler(final Field f, final Object o, final Tunable t) {
		final Class<?> type = f.getType();

		if (type == String.class)
			return new StringCLHandler(f,o,t);
		else if (type == boolean.class || type == Boolean.class)
			return new BooleanCLHandler(f,o,t);
		else if (type == int.class || type == Integer.class)
			return new IntCLHandler(f,o,t);
		else if (type == float.class || type == Float.class)
			return new IntCLHandler(f,o,t);
		else if (type == long.class || type == Long.class)
			return new IntCLHandler(f,o,t);
		else if (type == double.class || type == Double.class)
			return new IntCLHandler(f,o,t);
		else if (type == BoundedDouble.class)
			return new BoundedCLHandler<BoundedDouble>(f,o,t);
		else if (type == BoundedInteger.class)
			return new BoundedCLHandler<BoundedInteger>(f,o,t);
		else if (type == BoundedFloat.class)
			return new BoundedCLHandler<BoundedFloat>(f,o,t);
		else if (type == BoundedLong.class)
			return new BoundedCLHandler<BoundedLong>(f,o,t);
		else if (type == ListSingleSelection.class)
			return new ListSingleSelectionCLHandler<Object>(f,o,t);
		else if (type == ListMultipleSelection.class)
			return new ListMultipleSelectionCLHandler<Object>(f,o,t);
		else if (type == File.class)
			return new FileCLHandler(f,o,t);
		else if (type == URL.class)
			return new URLCLHandler(f,o,t);
		else if (type == InputStream.class)
			return new InputStreamCLHandler(f,o,t);
		else
			return null;
	}

}