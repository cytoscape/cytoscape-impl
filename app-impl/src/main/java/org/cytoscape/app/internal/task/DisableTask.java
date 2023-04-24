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
	public String error = null;

  App appObject = null;

	public DisableTask(final AppManager appManager) {
		super(appManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (app == null) {
			error = "App name not provided";
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "App name not provided");
			return;
		}
		taskMonitor.setTitle("Disabling app "+app);

		updateApps();
		appObject = getApp(app);
		if (appObject == null) {
			error = "Can't find app '"+app+"'";
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, error);
			return;
		}
		appManager.disableApp(appObject);
		updateApps();
		String msg = "App '"+app+"' disabled";
		taskMonitor.showMessage(TaskMonitor.Level.INFO, msg);
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
        if (error != null)
          return "{\"error\": \""+(R)error+"\"}" ;
				return "{\"appName\": \""+app+"\"}";
			};
			return (R)res;
		} else if (type.equals(String.class)) {
      // TODO: query the app manager to see if the app was installed?
			return (R)error;
		}
		return null;
	}

}
