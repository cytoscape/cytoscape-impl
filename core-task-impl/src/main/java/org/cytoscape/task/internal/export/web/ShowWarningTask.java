package org.cytoscape.task.internal.export.web;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 * Show warning message if session contains too big network.
 */
public class ShowWarningTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() {
		return "Export as Web Session";
	}

	@Tunable(description = "<html>Current session contains large network(s)<br />"
			+ "Cytoscape.js may not fast enough to visualize such networks.<br />"
			+ "Do you still want to export?</html>", params = "ForceSetDirectly=true;ForceSetTitle=Export as Web Session")
	public boolean showWarning = true;

	private Task task;

	public ShowWarningTask(Task exportTask) {
		this.task = exportTask;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (showWarning) {
			insertTasksAfterCurrentTask(task);
		}
	}
}
