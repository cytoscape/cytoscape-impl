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
import org.cytoscape.work.json.JSONResult;

public class ListAvailableTask extends AbstractAppTask implements ObservableTask {
	public ListAvailableTask(final AppManager appManager) {
		super(appManager);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Listing available apps");
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
				int count = webAppList.size();
				int index = 0;
				for (WebApp app: webAppList) {
					stringBuilder.append("{\"fullName\": \""+app.getFullName()+"\",");
					stringBuilder.append("\"name\": \""+app.getName()+"\",");
					stringBuilder.append("\"description\": \""+app.getDescription()+"\",");
					stringBuilder.append("\"details\": \""+app.getDetails()+"\"}");
					if (index < count)
						stringBuilder.append(",");
					index++;
				}
				stringBuilder.append("]");
				return stringBuilder.toString();
			};
			return (R)res;
		} else if (type.equals(String.class)) {
			List<String> appList = new ArrayList<String>(webAppList.size());
			Map<String,WebApp> appMap = new HashMap<String,WebApp>();
			for (WebApp app: webAppList) {
				appList.add(app.getFullName().toLowerCase());
				appMap.put(app.getFullName().toLowerCase(), app);
			}
			Collections.sort(appList);
			String list = "";
			for (String app: appList) {
				WebApp thisApp = appMap.get(app);
				list += "name: "+thisApp.getFullName()+", version: "+getVersion(thisApp)+"\n";
			}
			return (R)list;
		} else if (type.equals(List.class)) {
			List<String> list = new ArrayList<>();
			for (WebApp app: webAppList) {
				list.add(app.getFullName());
			}
			return (R)list;
		}
		return null;
	}

}
