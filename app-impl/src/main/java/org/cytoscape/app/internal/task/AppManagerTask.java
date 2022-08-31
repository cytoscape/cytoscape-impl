package org.cytoscape.app.internal.task;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;
import java.nio.charset.Charset;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.WebApp;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class AppManagerTask extends AbstractAppTask implements ObservableTask {
	final CyServiceRegistrar serviceRegistrar;
	private final CytoPanel cytoPanelWest;
	private static final String APP_MANAGER_DIR = "appManager/appmanager_v2.html";
	private String url = null;

	@Tunable (description="App name", context="nogui")
	public String app;

	@Tunable (description="Use CyBrowser if installed", context="nogui")
	public boolean useCybrowser = true;

	public AppManagerTask(final AppManager appManager, CyServiceRegistrar serviceRegistrar, CySwingApplication swingApplication) {
		super(appManager);
		cytoPanelWest = swingApplication.getCytoPanel(CytoPanelName.WEST);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		StringBuilder contentBuilder = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new InputStreamReader(AppManagerTask.class.getClassLoader().getResourceAsStream("/appmanager_v2.html"), Charset.forName("UTF-8").newDecoder()));
		    String str;
		    while ((str = in.readLine()) != null) {
		        contentBuilder.append(str);
		    }
		    in.close();
		} catch (IOException e) {
		}
		String content = contentBuilder.toString();

		final CyApplicationConfiguration applicationCfg = serviceRegistrar.getService(CyApplicationConfiguration.class);
		String APP_MANAGER = "file:///" + (applicationCfg.getConfigurationDirectoryLocation()).toString() + "/" + APP_MANAGER_DIR;
		WebApp webApp = null;
		if (app != null) {
			webApp = getWebApp(app);
			url = APP_MANAGER+"apps/"+app;
		} else {
			url = APP_MANAGER;
		}

		App cyBrowser = getApp("cybrowser");
		if (useCybrowser == true && cyBrowser != null && cyBrowser.getStatus() == App.AppStatus.INSTALLED) {
			CommandExecutorTaskFactory commandTF = serviceRegistrar.getService(CommandExecutorTaskFactory.class);
			TaskManager<?,?> taskManager = serviceRegistrar.getService(TaskManager.class);
			Map<String, Object> args = new HashMap<>();
			//args.put("url",url);
			args.put("text", content);
			args.put("id","App Manager");
			args.put("title","App Manager");
			args.put("panel","WEST");
			TaskIterator ti = commandTF.createTaskIterator("cybrowser","show",args, null);
			taskManager.execute(ti);
		} else {
			OpenBrowser openBrowser = serviceRegistrar.getService(OpenBrowser.class);
			openBrowser.openURL(url);
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
			String res = "Opened url: "+url;
			return (R)res;
		}
		return null;
	}

}
