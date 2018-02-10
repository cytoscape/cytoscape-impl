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

public class DisableTask extends AbstractAppTask implements ObservableTask {
	@Tunable(description="Name of app to disable", 
	         longDescription="The name of the app to disable",
	         exampleStringValue="stringApp",
	         context="nogui", required=true)
	public String app = null;

	public DisableTask(final AppManager appManager) {
		super(appManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (app == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "App name not provided");
			return;
		}
		taskMonitor.setTitle("Disabling app "+app);

		App appObject = getApp(app);
		if (appObject == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "Can't find app '"+app+"'");
			return;
		}
		appManager.disableApp(appObject);
		updateApps();
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "App '"+app+"' disabled");
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
				return "{\"app\": \""+app+"\"}";
			};
			return (R)res;
		} else if (type.equals(String.class)) {
			String res = "App "+app+" disabled";
			return (R)res;
		}
		return null;
	}

}
