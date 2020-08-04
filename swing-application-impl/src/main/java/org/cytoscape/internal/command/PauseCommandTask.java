package org.cytoscape.internal.command;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.LookAndFeelUtil;
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
		showDialog(parent, message);
		taskMonitor.showMessage(TaskMonitor.Level.INFO, "continuing");
	}
	
	
	private static void showDialog(JFrame parent, String message) {
		// Can't use JOptionPane because it doesn't work when run from automation script (CYTOSCAPE-12730).
		JLabel label = new JLabel(message);
		JButton okButton = new JButton("OK");
		JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, null);
		
		JPanel bodyPanel = new JPanel(new BorderLayout());
		bodyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		bodyPanel.add(label, BorderLayout.CENTER);
		bodyPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		JDialog dialog = new JDialog(parent);
		dialog.getContentPane().add(bodyPanel);
		
		okButton.addActionListener(e -> dialog.dispose());
		
		dialog.setTitle("Paused");
		dialog.setMinimumSize(new Dimension(200, 100));
		dialog.setLocationRelativeTo(parent);
		dialog.setModal(true);
		dialog.pack();
		dialog.setVisible(true);
	}
}
