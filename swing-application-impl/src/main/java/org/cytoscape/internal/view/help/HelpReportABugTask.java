package org.cytoscape.internal.view.help;

import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class HelpReportABugTask extends AbstractTask{

	private String bugReport = "http://chianti.ucsd.edu/cyto_web/bugreport/bugreport.php";
	private OpenBrowser openBrowser;

	public HelpReportABugTask(OpenBrowser openBrowser) {
		this.openBrowser = openBrowser;
	}

	public void run(TaskMonitor tm) {
		openBrowser.openURL(bugReport);
	}

}
