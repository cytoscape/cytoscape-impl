package org.cytoscape.internal.view.help;

import org.cytoscape.application.CyVersion;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class HelpReportABugTask extends AbstractTask{

	private String bugReportURL = "http://chianti.ucsd.edu/cyto_web/bugreport/bugreport.php";
	private OpenBrowser openBrowser;
	private final CyVersion cyVersion;
	
	public HelpReportABugTask(OpenBrowser openBrowser,  CyVersion cyVersion) {
		this.openBrowser = openBrowser;
		this.cyVersion = cyVersion;
	}

	public void run(TaskMonitor tm) {
		// get OS string
		String os_str = System.getProperty("os.name")+ "_"+ System.getProperty("os.version");
		os_str = os_str.replace(" ", "_");
		
		bugReportURL = "http://chianti.ucsd.edu/cyto_web/bugreport/bugreport.php?cyversion="+cyVersion.getVersion()+"&os="+os_str;
		openBrowser.openURL(bugReportURL);
	}

}
