package org.cytoscape.log.internal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Map;

import org.cytoscape.application.swing.CySwingApplication;

class ConsoleDialog extends JDialog
{
	final SimpleLogViewer simpleLogViewer;
	final AdvancedLogViewer advancedLogViewer;
	final JCheckBox viewSelection;
	final JPanel contents;

	public ConsoleDialog(	CySwingApplication app,
				SimpleLogViewer simpleLogViewer,
				AdvancedLogViewer advancedLogViewer)
	{
		super(app.getJFrame(), "Console");
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setPreferredSize(new Dimension(850, 400));

		this.advancedLogViewer = advancedLogViewer;
		this.simpleLogViewer = simpleLogViewer;

		contents = new JPanel(new CardLayout());
		contents.add(simpleLogViewer.getComponent(), "simple");
		contents.add(advancedLogViewer.getComponent(), "advanced");

		viewSelection = new JCheckBox("Show All Messages");
		viewSelection.addActionListener(new ViewSelectionAction());

		getContentPane().add(contents, BorderLayout.CENTER);
		getContentPane().add(viewSelection, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	class ViewSelectionAction implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			CardLayout cl = (CardLayout) contents.getLayout();
			if (viewSelection.isSelected())
				cl.show(contents, "advanced");
			else
				cl.show(contents, "simple");
		}
	}
}
