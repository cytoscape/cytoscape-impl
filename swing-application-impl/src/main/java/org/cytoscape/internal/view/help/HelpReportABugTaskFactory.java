package org.cytoscape.internal.view.help;

import org.cytoscape.application.CyVersion;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class HelpReportABugTaskFactory extends AbstractTaskFactory {

	private OpenBrowser openBrowser;
	private final CyVersion cyVersion;
	public HelpReportABugTaskFactory(OpenBrowser openBrowser,  CyVersion cyVersion) {
		this.openBrowser = openBrowser;
		this.cyVersion = cyVersion;
	}
	@Override
	public TaskIterator createTaskIterator() {

		return new TaskIterator(new HelpReportABugTask(openBrowser, cyVersion));
	}

}
