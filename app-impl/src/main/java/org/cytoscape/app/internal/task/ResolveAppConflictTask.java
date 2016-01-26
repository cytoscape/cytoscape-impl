package org.cytoscape.app.internal.task;

import java.util.Collection;
import java.util.Map;

import javax.swing.JOptionPane;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class ResolveAppConflictTask extends AbstractTask {
	
	@Tunable
	public final AppConflict conflict;
	
	private final Collection<App> appsToInstall;
	private final Map<App, App> appsToReplace;
	private final AppManager appManager;
	private int replaceApps = JOptionPane.YES_OPTION;

	
	public ResolveAppConflictTask(final Collection<App> appsToInstall, final Map<App,App> appsToReplace, final AppManager appManager) {
		this.appsToInstall = appsToInstall;
		this.appsToReplace = appsToReplace;
		this.appManager = appManager;
		this.conflict = new AppConflict();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// TODO Auto-generated method stub
		if (replaceApps == JOptionPane.OK_OPTION) {
			insertTasksAfterCurrentTask(new InstallAppsTask(appsToInstall, appsToReplace, appManager));
		}
	}
	
	public class AppConflict {
		private AppConflict(){};
		public Map<App, App> getAppsToReplace() {
			return ResolveAppConflictTask.this.appsToReplace;
		}
		
		
		public int getReplaceApps() {
			return ResolveAppConflictTask.this.replaceApps;
		}
		
		public void setReplaceApps(int replaceApps) {
			ResolveAppConflictTask.this.replaceApps = replaceApps;
		}
	}

}
