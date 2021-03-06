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
import org.cytoscape.app.internal.net.Update;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebApp.Release;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class UpdateTask extends AbstractAppTask implements ObservableTask {
	@Tunable(description="Name of app to update", 
	         longDescription="The name of the app to uninstall or ```all`` to update all apps.",
	         exampleStringValue="stringApp",
	         context="nogui", required=true)
	public String app = null;

	final UpdateManager updateManager;
	List<Update> updateList;

	public UpdateTask(final AppManager appManager, final UpdateManager updateManager) {
		super(appManager);
		this.updateManager = updateManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (app == null) {
			taskMonitor.showMessage(TaskMonitor.Level.ERROR, "App name not provided");
			return;
		}
		if (app.equals("all")) {
		}

		updateList = new ArrayList<>();

		Set<Update> updates = updateManager.getUpdates();
		if (app.equals("all")) {
			updateList.addAll(updates);
		} else {
			for (Update update: updates) {
				App appObject = update.getApp();
				if (appObject.getAppName().equalsIgnoreCase(app))
					updateList.add(update);
			}
		}

		taskMonitor.setTitle("Updating apps");
		insertTasksAfterCurrentTask(new InstallUpdatesTask(updates, appManager));
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
				String str = "[";
				int count = updateList.size();
				int index = 0;
				for (Update update: updateList) {
					App appObject = update.getApp();
					str += "{\"appName\": \""+appObject.getAppName()+"\"";
					str += ",\"version\": \""+appObject.getVersion()+"\"}";
					if (index < updateList.size())
						str += ",";
					index++;
				}
				str += "]";
				return str;
			};
			return (R)res;
		} else if (type.equals(String.class)) {
			String res = "Updated apps:\n";
			for (Update update: updateList) {
				res += "    "+update.getApp().getAppName()+"\n";
			}
			return (R)res;
		}
		return null;
	}
}
