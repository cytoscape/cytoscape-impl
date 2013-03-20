package org.cytoscape.app.internal.tunable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.cytoscape.app.internal.task.ResolveAppInstallationConflictTask.AppInstallationConflict;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandler;
import org.cytoscape.work.swing.GUITunableHandlerFactory;


public class AppInstallationConflictHandlerFactory implements GUITunableHandlerFactory {

	public GUITunableHandler createTunableHandler(Field field, Object instance, Tunable t) {
		if (!AppInstallationConflict.class.isAssignableFrom(field.getType()))
			return null;

		return new AppInstallationConflictHandler(field, instance, t);
	}

	public GUITunableHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if (!AppInstallationConflict.class.isAssignableFrom(getter.getReturnType()))
			return null;

		return new AppInstallationConflictHandler(getter, setter, instance, tunable);
	}

}
