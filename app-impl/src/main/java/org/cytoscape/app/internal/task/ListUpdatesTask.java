package org.cytoscape.app.internal.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.Update;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.json.JSONResult;

public class ListUpdatesTask extends AbstractAppTask implements ObservableTask {
	private Set<Update> updates;

	public ListUpdatesTask(final AppManager appManager) {
		super(appManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Listing available apps");
		WebQuerier webQuerier = appManager.getWebQuerier();
		Set<App> apps = appManager.getInstalledApps();
		updates = webQuerier.checkForUpdates(apps, appManager);
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(List.class, String.class, JSONResult.class);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> {
				StringBuilder stringBuilder = new StringBuilder("[");
				int count = updates.size();
				int index = 0;
				for (Update update: updates) {
					App app = update.getApp();
					stringBuilder.append("{\"appName\": \""+app.getAppName()+"\",");
					stringBuilder.append("\"version\": \""+app.getVersion()+"\",");
					stringBuilder.append("\"new version\": \""+update.getUpdateVersion()+"\",");
					if (index < count)
						stringBuilder.append(",");
					index++;
				}
				stringBuilder.append("]");
				return stringBuilder.toString();
			};
			return (R)res;
		} else if (type.equals(String.class)) {
			List<String> appList = new ArrayList<String>(updates.size());
			for (Update update: updates) {
				App app = update.getApp();
				appList.add("name: "+app.getAppName()+
				            ", current version: "+app.getVersion()+
				            ", new version: "+update.getUpdateVersion());
			}
			Collections.sort(appList);
			String list = "";
			for (String app: appList) {
				list += app+"\n";
			}
			return (R)list;
		} else if (type.equals(List.class)) {
			List<String> list = new ArrayList<>();
			for (Update update: updates) {
				list.add(update.getApp().getAppName());
			}
			return (R)list;
		}
		return null;
	}

}
