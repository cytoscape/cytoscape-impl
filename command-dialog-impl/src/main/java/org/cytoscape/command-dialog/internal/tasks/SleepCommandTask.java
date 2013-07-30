package org.cytoscape.commandDialog.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class SleepCommandTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() { return "Sleeping..."; }

	@Tunable(description="Duration of sleep in seconds")
	public double duration;

	public SleepCommandTask() {
		super();
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		if (duration != 0d) {
			arg0.showMessage(TaskMonitor.Level.INFO, "Sleeping for "+duration+" seconds");
			Thread.sleep((long)duration*1000);
			arg0.showMessage(TaskMonitor.Level.INFO, "Slept for "+duration+" seconds");
		} 

	}
}
