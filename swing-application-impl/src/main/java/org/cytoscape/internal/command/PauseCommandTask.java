package org.cytoscape.internal.command;

import javax.swing.JFrame;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.MessageDialogs;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class PauseCommandTask extends AbstractTask {
	
	private final CySwingApplication swingApplication;

	@ProvidesTitle
	public String getTitle() { return "Exiting Cytoscape"; }

	@Tunable (description="Message to show user (will wait until user responds)", exampleStringValue="Press OK to continue")
	public String message = null;

	public PauseCommandTask(CySwingApplication swingApplication) {
		this.swingApplication = swingApplication;
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		if (message == null || message.isEmpty())
			message = "Press OK to continue";
		
		JFrame parent = swingApplication.getJFrame();
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "Paused...");
		
		MessageDialogs.showMessageDialog(parent, "Paused", message);

		taskMonitor.showMessage(TaskMonitor.Level.INFO, "continuing");
	}
	
}
