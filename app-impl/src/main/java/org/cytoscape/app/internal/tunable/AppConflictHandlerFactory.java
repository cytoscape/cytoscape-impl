package org.cytoscape.app.internal.tunable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.app.internal.task.ResolveAppConflictTask.AppConflict;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandler;
import org.cytoscape.work.swing.GUITunableHandlerFactory;


public class AppConflictHandlerFactory implements GUITunableHandlerFactory {

	public GUITunableHandler createTunableHandler(Field field, Object instance, Tunable t) {
		if (!AppConflict.class.isAssignableFrom(field.getType()))
			return null;

		return new AppConflictHandler(field, instance, t);
	}

	public GUITunableHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if (!AppConflict.class.isAssignableFrom(getter.getReturnType()))
			return null;

		return new AppConflictHandler(getter, setter, instance, tunable);
	}

}
