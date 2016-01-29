package org.cytoscape.app.internal.tunable;

import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.task.ResolveAppConflictTask.AppConflict;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConflictHandler extends AbstractGUITunableHandler implements DirectlyPresentableTunableHandler {

	private static final Logger logger = LoggerFactory.getLogger(AppConflictHandler.class);
	
	protected AppConflictHandler(Field field, Object instance, Tunable tunable) {
		super(field, instance, tunable);
	}

	protected AppConflictHandler(Method getter, Method setter,
			Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	@Override
	public boolean setTunableDirectly(Window possibleParent) {
		try {
			AppConflict conflict = (AppConflict) getValue();
			Map<App, App> appsToReplace = conflict.getAppsToReplace();
			String text = "The following "+ (appsToReplace.size() == 1 ? "app" : "apps") + " will be replaced:\n\n";
			for(Entry<App, App> entry: appsToReplace.entrySet()) {
				text += entry.getKey().getAppName() + " (new version: " +
						entry.getKey().getVersion() + ", " + entry.getValue().getVersion() +
						" currently installed)\n";
			}
			text += "\nContinue?";
			int response = JOptionPane.showConfirmDialog(possibleParent, text, 
					"Replace "+ (appsToReplace.size() == 1 ? "App" : "Apps"), JOptionPane.OK_CANCEL_OPTION);
			conflict.setReplaceApps(response);
		} catch (IllegalAccessException e) {
			logger.warn("Error accessing conflict object",e);
		} catch (InvocationTargetException e) {
			logger.warn("Exception thrown by conflict object",e);
		}
		return true;
	}

	@Override
	public boolean isForcedToSetDirectly() {
		return true;
	}

	@Override
	public void handle() {
		// stub method
	}

}
