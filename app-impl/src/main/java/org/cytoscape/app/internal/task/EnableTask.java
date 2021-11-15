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

public class EnableTask extends AbstractAppTask implements ObservableTask {
	@Tunable(description="Name of app to enable", 
	         longDescription="The name of the app to enable",
	         exampleStringValue="stringApp",
	         context="nogui", required=true)
	public String app = null;
	public String error = null;
	App appObject = null;

	public EnableTask(final AppManager appManager) {
		super(appManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (app == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "App name not provided");
			return;
		}
		taskMonitor.setTitle("Enabling app "+app);
		appObject = getApp(app);
		// System.out.println("App "+appObject.getAppName()+" version "+appObject.getVersion());
		if (appObject == null) {
			error = "Can't find app '"+app+"'";
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, error);
			return;
		}
		if (!appObject.getStatus().equals(App.AppStatus.DISABLED)) {
			error = "App '"+app+"' is not disabled";
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, error);
			return;
		}
		appManager.installApp(appObject);
		updateApps();
		error = "App '"+app+"' re-enabled";
		taskMonitor.showMessage(TaskMonitor.Level.INFO, error);
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
				return "{\"appName\": \""+app+"\"}";
			};
			return (R)res;
		} else if (type.equals(String.class)) {
      if (appObject == null)
        return null;
			String res = error;
			return (R)res;
		}
		return null;
	}

}
