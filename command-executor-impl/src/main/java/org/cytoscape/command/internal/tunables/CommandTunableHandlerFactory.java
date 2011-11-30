package org.cytoscape.command.internal.tunables;


import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedFloat;
import org.cytoscape.work.util.BoundedInteger;
import org.cytoscape.work.util.BoundedLong;
import org.cytoscape.work.util.ListMultipleSelection;
import org.cytoscape.work.util.ListSingleSelection;


public class CommandTunableHandlerFactory implements TunableHandlerFactory<StringTunableHandler> {


	public StringTunableHandler createTunableHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		final Class<?> type = getter.getReturnType();

		if (type == Boolean.class || type == boolean.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == String.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == Integer.class || type == int.class)
			return new IntTunableHandler(getter, setter, instance, tunable);
		if (type == Double.class || type == double.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == Float.class || type == float.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == Long.class || type == long.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == BoundedInteger.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == BoundedLong.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == BoundedFloat.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == BoundedDouble.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == ListSingleSelection.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == ListMultipleSelection.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == File.class)
			return new FileTunableHandler(getter, setter, instance, tunable);
		if (type == URL.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);
		if (type == InputStream.class)
			return new DummyTunableHandler(getter, setter, instance, tunable);

		return null;
	}

	public StringTunableHandler createTunableHandler(final Field field, final Object instance, final Tunable tunable) {
		final Class<?> type = field.getType();

		if (type == Boolean.class || type == boolean.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == String.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == Integer.class || type == int.class)
			return new IntTunableHandler(field, instance, tunable);
		if (type == Double.class || type == double.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == Float.class || type == float.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == Long.class || type == long.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == BoundedInteger.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == BoundedLong.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == BoundedFloat.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == BoundedDouble.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == ListSingleSelection.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == ListMultipleSelection.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == File.class)
			return new FileTunableHandler(field, instance, tunable);
		if (type == URL.class)
			return new DummyTunableHandler(field, instance, tunable);
		if (type == InputStream.class)
			return new DummyTunableHandler(field, instance, tunable);

		return null;
	}
}
