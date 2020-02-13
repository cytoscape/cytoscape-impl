package org.cytoscape.task.internal.help;

import org.cytoscape.application.CyVersion;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class HelpTask extends AbstractTask {
	
	private final String URL_TEMPLATE = "https://manual.cytoscape.org/en/%s.%s.%s/Quick_Tour_of_Cytoscape.html";
	private final CyServiceRegistrar serviceRegistrar;

	public HelpTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		var cyVersion = serviceRegistrar.getService(CyVersion.class);
		var major = cyVersion.getMajorVersion();
		var minor = cyVersion.getMinorVersion();
		var fix = cyVersion.getBugFixVersion();
		var url = String.format(URL_TEMPLATE, major, minor, fix);
		
		try {
			serviceRegistrar.getService(OpenBrowser.class).openURL(url);
		} catch (Exception err) {
			System.out.println("Unable to open browser for " + url.toString());
		}
	}
}
