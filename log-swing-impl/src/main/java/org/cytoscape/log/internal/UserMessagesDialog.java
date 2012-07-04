package org.cytoscape.log.internal;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Map;

import org.cytoscape.application.swing.CySwingApplication;

class UserMessagesDialog {
	final JDialog dialog;
	final LogViewer logViewer;

	public UserMessagesDialog(final CySwingApplication app, final Map<String,String> logViewerConfig) {
		dialog = new JDialog(app.getJFrame(), "User Messages");
		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		dialog.setPreferredSize(new Dimension(850, 400));
		dialog.setModal(false);
		dialog.setAlwaysOnTop(false);

		logViewer = new LogViewer(logViewerConfig);
		dialog.add(logViewer.getComponent(), BorderLayout.CENTER);

		JButton clearBtn = new JButton("Clear");
		clearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logViewer.clear();
			}
		});
		JPanel bottomBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottomBtns.add(clearBtn);
		dialog.add(bottomBtns, BorderLayout.SOUTH);

		dialog.pack();
	}

	public void open() {
		dialog.setVisible(true);
	}

	public void addMessage(final String level, final String message) {
		logViewer.append(level, message, "");
		logViewer.scrollToBottom();
	}
}
