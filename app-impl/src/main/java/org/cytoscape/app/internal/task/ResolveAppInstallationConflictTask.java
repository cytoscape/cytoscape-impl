package org.cytoscape.app.internal.task;

import javax.swing.JOptionPane;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ResolveAppInstallationConflictTask extends AbstractTask {
	
	@Tunable
	public final AppInstallationConflict conflict;
	
	private final App appToInstall;
	private final App conflictingApp;
	private final AppManager appManager;
	private int replaceApp = JOptionPane.YES_OPTION;
	
	public ResolveAppInstallationConflictTask(final App appToInstall, final App conflictingApp, final AppManager appManager) {
		this.appToInstall = appToInstall;
		this.conflictingApp = conflictingApp;
		this.appManager = appManager;
		conflict = new AppInstallationConflict();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (replaceApp == JOptionPane.YES_OPTION) {
			appManager.installApp(appToInstall);
			appManager.uninstallApp(conflictingApp);
		}
		
		else if (replaceApp == JOptionPane.NO_OPTION) {
			// Install both
			appManager.installApp(appToInstall);
		}
		
		else if (replaceApp == JOptionPane.CANCEL_OPTION) {
			// Do nothing
		}
	}
	
	public class AppInstallationConflict {
		private AppInstallationConflict(){};
		public App getAppToInstall() {
			return ResolveAppInstallationConflictTask.this.appToInstall;
		}
		
		public App getConflictingApp() {
			return ResolveAppInstallationConflictTask.this.conflictingApp;
		}
		
		public int getReplaceApp() {
			return ResolveAppInstallationConflictTask.this.replaceApp;
		}
		
		public void setReplaceApp(int replaceApp) {
			ResolveAppInstallationConflictTask.this.replaceApp = replaceApp;
		}
	}
}
