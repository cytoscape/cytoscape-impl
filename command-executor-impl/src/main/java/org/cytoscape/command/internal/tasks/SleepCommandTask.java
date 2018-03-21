package org.cytoscape.command.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class SleepCommandTask extends AbstractTask {

	@ProvidesTitle
	public String getTitle() { return "Sleeping..."; }

	@Tunable(description="Duration of sleep in seconds",
	         longDescription="Enter the time in seconds to sleep",
	         exampleStringValue="5")
	public double duration;


	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (duration != 0d) {
			tm.showMessage(TaskMonitor.Level.INFO, "Sleeping for "+duration+" seconds");
			Thread.sleep((long)duration*1000);
			tm.showMessage(TaskMonitor.Level.INFO, "Slept for "+duration+" seconds");
		}
	}
}
