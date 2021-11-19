package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebApp.Release;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class InstallTask extends AbstractAppTask implements ObservableTask {
	@Tunable(description="Name of app to install", 
	         longDescription="The name of the app to install",
	         exampleStringValue="stringApp",
	         context="nogui", required=false)
	public String app = null;
	String error = null;

	@Tunable(description="Jar file",
	         longDescription="Location of file containing the app jar",
	         exampleStringValue="/tmp/stringApp.jar",
	         context="nogui", required=false)
	public File file = null;

  WebApp appObject = null;

	public InstallTask(final AppManager appManager) {
		super(appManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (app == null && file == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Neither app name nor file provided");
			return;
		}
		taskMonitor.setTitle("Installing app "+app);
		if (file != null) {
			InstallAppsFromFileTask installTask =
					new InstallAppsFromFileTask(Collections.singletonList(file), appManager, false);
			insertTasksAfterCurrentTask(installTask);
		} else {
			appObject = getWebApp(app);
			if (appObject == null) {
				error = "Can't find app '"+app+"'";
				taskMonitor.showMessage(TaskMonitor.Level.ERROR, error);
				return;
			}

			InstallAppsFromWebAppTask installTask = 
							new InstallAppsFromWebAppTask(Collections.singletonList(appObject), appManager, false);
			insertTasksAfterCurrentTask(installTask);
		}
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				return "{}";
			};
			return (R)res;
		} else if (type.equals(String.class)) {
      // TODO: query the app manager to see if the app was installed?
      if (file != null)
        return null; // We really don't know if this was successful or not
      if (error != null)
        return (R)error;
			String res = "App "+app+" installed";
			return (R)res;
		}
		return null;
	}

}
