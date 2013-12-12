package org.cytoscape.commandDialog.internal.tasks;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class PauseCommandTask extends AbstractTask {
	JFrame parent;

	@ProvidesTitle
	public String getTitle() { return "Exiting Cytoscape"; }

	@Tunable (description="Message to show user (will wait until user responds)")
	public String message = null;

	public PauseCommandTask(JFrame parent) {
		super();
		this.parent = parent;
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		if (message==null || message.length() == 0)
			message = "Press OK to continue";

		JOptionPane.showMessageDialog(parent, message, "Paused", JOptionPane.PLAIN_MESSAGE);
		arg0.showMessage(TaskMonitor.Level.INFO, "Paused...continuing");
	}
}
