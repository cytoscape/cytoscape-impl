package org.cytoscape.app.internal.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.app.internal.ui.downloadsites.ManageDownloadSitesDialog;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2021 The Cytoscape Consortium
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

/**
 * This class represents the App Manager dialog window.
 */
@SuppressWarnings("serial")
public class AppManagerDialog extends JDialog {

	private JTabbedPane mainTabbedPane;

	private ManageDownloadSitesDialog manageDownloadSitesDialog;
	private DownloadSitesManager downloadSitesManager;

	private final AppManager appManager;
	private final UpdateManager updateManager;
	private final CyServiceRegistrar serviceRegistrar;

	public AppManagerDialog(
			final Window parent,
			final AppManager appManager,
			final DownloadSitesManager downloadSitesManager,
			final UpdateManager updateManager,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(parent, ModalityType.APPLICATION_MODAL);

		this.appManager = appManager;
		this.downloadSitesManager = downloadSitesManager;
		this.updateManager = updateManager;
		this.serviceRegistrar = serviceRegistrar;

		// Create new manage download sites dialog
		manageDownloadSitesDialog = new ManageDownloadSitesDialog(parent, downloadSitesManager);
		manageDownloadSitesDialog.setLocationRelativeTo(this);

		setLocationRelativeTo(parent);
		appManager.setAppManagerDialog(this);
	}

	private void initComponents() {
    	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("App Manager");


		final FileUtil fileUtil = serviceRegistrar.getService(FileUtil.class);
		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);

		mainTabbedPane = new JTabbedPane();

		final JButton closeButton = new JButton(new AbstractAction("Close") {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(null, closeButton, "App_Manager");

		final GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);


        LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), null, closeButton.getAction());

        pack();
    }

	public void changeTab(int index) {
		mainTabbedPane.setSelectedIndex(index);
	}

	public void showManageDownloadSitesDialog() {
		if (manageDownloadSitesDialog != null) {
			manageDownloadSitesDialog.setLocationRelativeTo(this);
			manageDownloadSitesDialog.setVisible(true);
		}
	}

	public void showNetworkError(String errorMessage) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> showNetworkError(errorMessage));
		} else {
		}
	}

	public void hideNetworkError() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(() -> hideNetworkError());
		} else {
		}
	}
}
