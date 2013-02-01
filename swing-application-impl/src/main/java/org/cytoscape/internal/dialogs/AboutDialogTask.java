package org.cytoscape.internal.dialogs;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRootPane;

import org.cytoscape.application.CyVersion;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class AboutDialogTask implements Task {

	private final CyVersion version;
	private JDialog dialog;

	public AboutDialogTask(final CyVersion version) {
		this.version = version;
	}
	
	@Override
	public synchronized void run(final TaskMonitor taskMonitor) throws Exception {
		if (dialog == null) {
			dialog = new JDialog();
			dialog.setTitle("About Cytoscape");
			dialog.setResizable(false);
			
			JRootPane pane = dialog.getRootPane();
			pane.setLayout(new GridBagLayout());
			
			ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("images/logo.png"));
			JLabel iconLabel = new JLabel(icon);
			
			JLabel productLabel = new JLabel("Cytoscape", JLabel.CENTER);
			Font font = productLabel.getFont();
			productLabel.setFont(font.deriveFont(Font.BOLD, (int) (font.getSize() * 2)));
			
			JLabel versionLabel = new JLabel(String.format("Version %s", version.getVersion()), JLabel.CENTER);
			
			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}
			});
			
			pane.setDefaultButton(okButton);
			pane.add(iconLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			pane.add(productLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			pane.add(versionLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			pane.add(okButton, new GridBagConstraints(0, 3, 1, 1, 1, 0, GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
			dialog.pack();
			final Dimension size = dialog.getSize();
			dialog.setSize(size.width * 2, size.height);
		}
		
		if (!dialog.isVisible()) {
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
		} else {
			dialog.toFront();
		}
	}

	@Override
	public void cancel() {
	}
}
