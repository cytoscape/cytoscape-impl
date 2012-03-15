package org.cytoscape.internal.commands;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.File;
import java.net.URL;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.work.util.*;


public class BasicArgHandlerFactory implements ArgHandlerFactory {
	public ArgHandler createTunableHandler(final Field field, final Object instance, final Tunable tunable) {
		return new BasicArgHandler(field, instance, tunable);
	}

	public ArgHandler createTunableHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		return new BasicArgHandler(getter, setter, instance, tunable);
	}
}
