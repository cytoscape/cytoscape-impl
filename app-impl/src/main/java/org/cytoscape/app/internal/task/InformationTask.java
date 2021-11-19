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
import org.cytoscape.app.internal.util.AppUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class InformationTask extends AbstractAppTask implements ObservableTask {
	@Tunable(description="App name", 
	         longDescription="The name of the app to get information on",
	         exampleStringValue="stringApp",
	         context="nogui", required=true)
	public String app = null;
	String error = null;
	WebApp webApp;

	public InformationTask(final AppManager appManager) {
		super(appManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (app == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "App name not provided");
			return;
		}
		taskMonitor.setTitle("Getting app "+app+" information");
		webApp = getWebApp(app);
		if (webApp == null) {
			error = "Can't find app '"+app+"'";
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, error);
			return;
		}
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Got information on '"+app+"'");
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
				String str = "{";
				str += "\"app\": \""+app+"\",";
				str += "\"descriptionName\": "+AppUtil.quote(webApp.getDescription())+",";
				str += "\"version\": \""+getVersion(webApp)+"\"}";
				return str;
			};
			return (R)res;
		} else if (type.equals(String.class)) {
      if (error != null)
        return (R)error;
			String res = "App: "+webApp.getName()+", description: "+
			             webApp.getDescription()+", version: "+getVersion(webApp);
			return (R)res;
		}
		return null;
	}

}
