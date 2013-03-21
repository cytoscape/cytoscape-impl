package org.cytoscape.app.internal.tunable;

import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.task.ResolveAppInstallationConflictTask.AppInstallationConflict;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppInstallationConflictHandler extends AbstractGUITunableHandler implements DirectlyPresentableTunableHandler {

	private static final Logger logger = LoggerFactory.getLogger(AppInstallationConflictHandler.class);
	
	protected AppInstallationConflictHandler(Field field, Object instance, Tunable tunable) {
		super(field, instance, tunable);
	}

	protected AppInstallationConflictHandler(Method getter, Method setter,
			Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	@Override
	public boolean setTunableDirectly(Window possibleParent) {
		try {
			AppInstallationConflict conflict = (AppInstallationConflict) getValue();
			App appToInstall = conflict.getAppToInstall();
			App conflictingApp = conflict.getConflictingApp();
			int response = JOptionPane.showConfirmDialog(possibleParent, "There is an app \"" + conflictingApp.getAppName()
					+ "\" with the same name. It is version " + conflictingApp.getVersion() + ", while the" 
					+ " one you're about to install is version " + appToInstall.getVersion() 
					+ ". Replace it?", "Replace App?", JOptionPane.YES_NO_CANCEL_OPTION);
			conflict.setReplaceApp(response);
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
