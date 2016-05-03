package org.cytoscape.log.internal;

/*
 * #%L
 * Cytoscape Log Swing Impl (log-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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

		logViewer = new LogViewer(logViewerConfig);
		dialog.add(logViewer.getComponent(), BorderLayout.CENTER);

		JButton clearBtn = new JButton("Clear");
		clearBtn.addActionListener(e -> logViewer.clear());
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
