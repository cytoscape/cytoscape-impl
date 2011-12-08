package org.cytoscape.work.internal.tunables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;

public class URLHandlerFactory implements GUITunableHandlerFactory<URLHandler> {
	
	private final DataSourceManager manager;

	public URLHandlerFactory(final DataSourceManager manager) {
		this.manager = manager;
	}

	public URLHandler createTunableHandler(Field field, Object instance, Tunable tunable) {
		if ( field.getType() != URL.class)
			return null;
		
		return new URLHandler(field, instance, tunable, manager);
	}

	public URLHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if ( getter.getReturnType() != URL.class)
			return null;
		
		return new URLHandler(getter, setter, instance, tunable, manager);
	}

}
