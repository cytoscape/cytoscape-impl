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
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebApp.Release;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class StatusTask extends AbstractAppTask implements ObservableTask {
	@Tunable(description="Name of app to get the status of", 
	         longDescription="The name of the app to get the status of",
	         exampleStringValue="stringApp",
	         context="nogui", required=true)
	public String app = null;
	String error = null;
	private App appObject = null;
	private AppStatus status;

	public StatusTask(final AppManager appManager) {
		super(appManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (app == null) {
      error = "App name not provided";
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, error);
			return;
		}
		taskMonitor.setTitle("Getting the status of app "+app);
		appObject = getApp(app);
		if (appObject == null) {
			error = "Can't find app '"+app+"'";
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, error);
			return;
		}
		status = appObject.getStatus();
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

				return "{\"appName\": \""+app+"\", \"status\": \""+status.toString()+"\" }";
			};
			return (R)res;
		} else if (type.equals(String.class)) {
      if (error != null)
        return (R)error;
			String res = "app: "+app+", status: "+status.toString();
			return (R)res;
		}
		return null;
	}

}
