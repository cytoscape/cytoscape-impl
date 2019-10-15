package org.cytoscape.internal.view.updater;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;

import javax.swing.*;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.ActionEvent;

import static javax.swing.GroupLayout.Alignment.CENTER;
import static javax.swing.GroupLayout.Alignment.LEADING;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class UpdatesDialog extends JDialog {

	private static final String DOWNLOAD_URL = "http://cytoscape.org/download.php";

	private JPanel statusPanel;
	private JCheckBox checkBox;
	private JButton downloadButton;
	private JButton closeButton;
	private final JLabel statusIconLabel = new JLabel();
	private final JLabel statusLabel = new JLabel();

	private final String thisVersion;
	private final String latestVersion;
	private final boolean hideOptionVisible;

	private final CyServiceRegistrar serviceRegistrar;

	public UpdatesDialog(final Window owner, final String thisVersion, final String latestVersion,
			final boolean hideOptionVisible, final CyServiceRegistrar serviceRegistrar) {
		super(owner);
		this.thisVersion = thisVersion;
		this.latestVersion = latestVersion;
		this.hideOptionVisible = hideOptionVisible;
		this.serviceRegistrar = serviceRegistrar;

		initComponents();
	}

	public boolean getHideStatus() {
		return getCheckBox().isSelected();
	}

	private void initComponents() {
		this.setResizable(false);
		this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);

		boolean downloadEnabled = false;

		if (thisVersion != null && latestVersion != null) {
			if (thisVersion.equals(latestVersion)) {
				statusIconLabel.setText(IconManager.ICON_INFO_CIRCLE);
				statusIconLabel.setForeground(LookAndFeelUtil.getInfoColor());
				statusLabel.setText("Cytoscape is up to date!");
			} else {
				if (thisVersion.contains("-")) {
					statusIconLabel.setText(IconManager.ICON_INFO_CIRCLE);
					statusIconLabel.setForeground(LookAndFeelUtil.getInfoColor());
					statusLabel.setText("This is a pre-release version of Cytoscape.");
				} else {
					statusIconLabel.setText(IconManager.ICON_ARROW_CIRCLE_DOWN);
					statusIconLabel.setForeground(LookAndFeelUtil.getInfoColor());
					statusLabel.setText("<html><p>A new version of Cytoscape is available!</p>"
							+ "<p>Would you like to download version " + latestVersion + "?</p></html>");
					downloadEnabled = true;
				}
			}
		} else {
			statusIconLabel.setText(IconManager.ICON_EXCLAMATION_TRIANGLE);
			statusIconLabel.setForeground(LookAndFeelUtil.getErrorColor());
			statusLabel.setText("Unable to get current version. Are you connected to the internet?");
		}

		final JPanel bottomPanel;

		if (downloadEnabled)
			bottomPanel = LookAndFeelUtil.createOkCancelPanel(getDownloadButton(), getCloseButton());
		else
			bottomPanel = LookAndFeelUtil.createOkCancelPanel(null, getCloseButton());

		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());

		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(getStatusPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getStatusPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(bottomPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE));

		if (downloadEnabled) {
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getDownloadButton().getAction(),
					getCloseButton().getAction());
			getRootPane().setDefaultButton(getDownloadButton());
			getDownloadButton().requestFocusInWindow();
		} else {
			LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), getCloseButton().getAction(),
					getCloseButton().getAction());
			getRootPane().setDefaultButton(getCloseButton());
		}
	}

	private JPanel getStatusPanel() {
		if (statusPanel == null) {
			statusPanel = new JPanel();
			statusPanel.setName("StatusPanel");

			statusIconLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(36.0f));

			final int hpad = 20;
			final int vpad = 40;

			final GroupLayout layout = new GroupLayout(statusPanel);
			statusPanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			layout.setHorizontalGroup(layout.createSequentialGroup().addGap(hpad)
					.addComponent(statusIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(layout.createParallelGroup(LEADING, true)
							.addComponent(statusLabel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
							.addComponent(getCheckBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
					.addGap(hpad, hpad, Short.MAX_VALUE));
			layout.setVerticalGroup(layout.createParallelGroup(CENTER, false).addGap(vpad)
					.addComponent(statusIconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGroup(layout.createSequentialGroup()
							.addComponent(statusLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(getCheckBox(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE))
					.addGap(vpad));
		}

		return statusPanel;
	}

	private JCheckBox getCheckBox() {
		if (checkBox == null) {
			checkBox = new JCheckBox("Do not remind me again for this version");
			checkBox.setHorizontalAlignment(SwingConstants.LEFT);
			LookAndFeelUtil.makeSmall(checkBox);
			checkBox.setVisible(hideOptionVisible);
		}

		return checkBox;
	}

	private JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton(new AbstractAction("Close") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
		}

		return closeButton;
	}

	private JButton getDownloadButton() {
		if (downloadButton == null) {
			downloadButton = new JButton(new AbstractAction("Download Now") {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
					serviceRegistrar.getService(OpenBrowser.class).openURL(DOWNLOAD_URL);
				}
			});
		}

		return downloadButton;
	}
}
