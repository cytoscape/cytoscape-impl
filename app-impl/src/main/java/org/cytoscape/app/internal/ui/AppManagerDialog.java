package org.cytoscape.app.internal.ui;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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
 * Copyright (C) 2008 - 2020 The Cytoscape Consortium
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

	private CheckForUpdatesPanel checkForUpdatesPanel;
	private CurrentlyInstalledAppsPanel currentlyInstalledAppsPanel;
	private InstallAppsPanel installAppsPanel;
	private JTabbedPane mainTabbedPane;
	private JLabel networkErrorLabel;

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

		initComponents();

		// Create new manage download sites dialog
		manageDownloadSitesDialog = new ManageDownloadSitesDialog(parent, downloadSitesManager);
		manageDownloadSitesDialog.setLocationRelativeTo(this);

		setLocationRelativeTo(parent);
		appManager.setAppManagerDialog(this);
	}
   
	private void initComponents() {
    	setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("App Manager");
    	
    	networkErrorLabel = new JLabel(
        		"Cannot access the App Store. Please check your internet connection.",
        		UIManager.getIcon("OptionPane.warningIcon"),
        		SwingConstants.LEFT);
		networkErrorLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		networkErrorLabel.setVisible(false);

		final FileUtil fileUtil = serviceRegistrar.getService(FileUtil.class);
		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		final IconManager iconManager = serviceRegistrar.getService(IconManager.class);

		installAppsPanel = new InstallAppsPanel(appManager, downloadSitesManager, fileUtil, iconManager, taskManager, this);
		installAppsPanel.setOpaque(!LookAndFeelUtil.isAquaLAF()); // Transparent if Aqua

		currentlyInstalledAppsPanel = new CurrentlyInstalledAppsPanel(appManager);
		currentlyInstalledAppsPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());

		checkForUpdatesPanel = new CheckForUpdatesPanel(appManager, downloadSitesManager, updateManager, taskManager, this);
		checkForUpdatesPanel.setOpaque(!LookAndFeelUtil.isAquaLAF());

		mainTabbedPane = new JTabbedPane();
		mainTabbedPane.addTab("Install Apps", installAppsPanel);
		mainTabbedPane.addTab("Currently Installed", currentlyInstalledAppsPanel);
		mainTabbedPane.addTab("Check for Updates", checkForUpdatesPanel);

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
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(networkErrorLabel)
        		.addComponent(mainTabbedPane, DEFAULT_SIZE, 640, Short.MAX_VALUE)
        		.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(networkErrorLabel)
				.addComponent(mainTabbedPane, DEFAULT_SIZE, 490, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
        
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

	public void showNetworkError() {
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(() -> showNetworkError());
		else
			networkErrorLabel.setVisible(true);
	}

	public void hideNetworkError() {
		if (!SwingUtilities.isEventDispatchThread())
			SwingUtilities.invokeLater(() -> hideNetworkError());
		else
			networkErrorLabel.setVisible(false);
	}
}
