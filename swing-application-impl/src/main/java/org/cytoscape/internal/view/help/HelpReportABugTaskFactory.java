package org.cytoscape.internal.view.help;

import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class HelpReportABugTaskFactory extends AbstractTaskFactory {

	private OpenBrowser openBrowser;

	public HelpReportABugTaskFactory(OpenBrowser openBrowser) {
		this.openBrowser = openBrowser;
	}
	@Override
	public TaskIterator createTaskIterator() {

		return new TaskIterator(new HelpReportABugTask(openBrowser));
	}

}
