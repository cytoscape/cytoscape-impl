package org.cytoscape.task.internal.help;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class HelpTask extends AbstractTask {
	
	private final String url;
	private final CyServiceRegistrar registrar;

	public HelpTask(CyServiceRegistrar serviceRegistrar, String link) {
		super();
		registrar = serviceRegistrar;
		url = link;
	}

	@Override
	public void run(TaskMonitor tm) {
		try {
			registrar.getService(OpenBrowser.class).openURL(url);
		} catch (Exception err) {
			System.out.println("Unable to open browser for " + url.toString());
		}
	}
}
