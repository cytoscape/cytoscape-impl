package org.cytoscape.log.internal;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author Pasteur
 */
class SimpleLogViewer
{
	final CytoStatusBar statusBar;
	final LogViewer logViewer;
	final JPanel contents;

	public SimpleLogViewer(CytoStatusBar statusBar, LogViewer logViewer)
	{
		this.statusBar = statusBar;
		this.logViewer = logViewer;
		contents = new JPanel();

		contents.setLayout(new BorderLayout());

		contents.add(logViewer.getComponent(), BorderLayout.CENTER);
		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton clearButton = new JButton("  Clear  ");
		clearButton.addActionListener(new ClearAction());
		buttons.add(clearButton);
		contents.add(buttons, BorderLayout.PAGE_END);
	}

	public JComponent getComponent()
	{
		return contents;
	}

	class ClearAction implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			logViewer.clear();
			statusBar.setMessage(null, null);
		}
	}


	public void append(String level, String message, String secondaryMessage)
	{
		logViewer.append(level, message, secondaryMessage);
	}
}
