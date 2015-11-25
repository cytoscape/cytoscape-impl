package org.cytoscape.internal.actions;

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

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update "Open Recent Session" menu.
 */
@SuppressWarnings("serial")
public class RecentSessionManager implements SessionLoadedListener, CyShutdownListener {
	
	private static final Logger logger = LoggerFactory.getLogger(RecentSessionManager.class);
	
	private static final String MENU_CATEGORY = "File.Open Recent";

	private final Set<OpenRecentSessionAction> currentMenuItems;
	private final ClearMenuAction clearMenuAction;
	
	private final CyServiceRegistrar serviceRegistrar;

	public RecentSessionManager(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.currentMenuItems = new HashSet<>();
		
		clearMenuAction = new ClearMenuAction();
		serviceRegistrar.registerAllServices(clearMenuAction, new Properties());
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateMenuItems();
			}
		});
	}

	private void updateMenuItems() {
		// Unregister services
		for (final OpenRecentSessionAction currentItem : currentMenuItems)
			serviceRegistrar.unregisterAllServices(currentItem);

		currentMenuItems.clear();

		final RecentlyOpenedTracker tracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
		final List<URL> urls = tracker.getRecentlyOpenedURLs();
		float gravity = 0.0f;

		for (final URL url : urls) {
			File file = null;
			
			try {
				URI uri = url.toURI();
				file = new File(uri);
			} catch (URISyntaxException e) {
				logger.error("Invalid file URL.", e);
				continue;
			}
			
			final OpenRecentSessionAction action = new OpenRecentSessionAction(gravity++, file);
			serviceRegistrar.registerService(action, CyAction.class, new Properties());
			currentMenuItems.add(action);
		}
		
		clearMenuAction.updateEnableState();
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateMenuItems();
			}
		});
	}
	
	@Override
	public void handleEvent(CyShutdownEvent e) {
		final RecentlyOpenedTracker tracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
		
		try {
			tracker.writeOut();
		} catch (FileNotFoundException ex) {
			logger.error("Could not save recently opened session file list.", ex);
		}
	}
	
	private final class OpenRecentSessionAction extends AbstractCyAction {
		
		private final File file;
		
		public OpenRecentSessionAction(float gravity, final File file) {
			super(file.getAbsolutePath());
			setPreferredMenu(MENU_CATEGORY);
			setMenuGravity(gravity);
			this.file = file;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (file.exists()) {
				final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
				final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
				
				if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
					openSession();
				else
					openSessionWithWarning();
			} else {
				final CySwingApplication swingApp = serviceRegistrar.getService(CySwingApplication.class);
				JOptionPane.showMessageDialog(
						swingApp.getJFrame(),
						"Session file not found:\n" + file.getAbsolutePath(),
						"File not Found",
						JOptionPane.WARNING_MESSAGE
				);
				
				final RecentlyOpenedTracker tracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
				
				try {
					tracker.remove(file.toURI().toURL());
					updateMenuItems();
				} catch (Exception ex) {
					logger.error("Error removing session file from RecentlyOpenedTracker.", ex);
				}
			}
		}
		
		private void openSession() {
			final OpenSessionTaskFactory taskFactory = serviceRegistrar.getService(OpenSessionTaskFactory.class);
			final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			taskManager.execute(taskFactory.createTaskIterator(file));
		}
		
		private void openSessionWithWarning() {
			final CySwingApplication swingApp = serviceRegistrar.getService(CySwingApplication.class);
			
			if (JOptionPane.showConfirmDialog(
					swingApp.getJFrame(),
					"Current session (all networks and tables) will be lost.\nDo you want to continue?",
					"Open Session",
					JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
				openSession();
		}
	}
	
	/**
	 * Menu action to clear the list of recent sessions.
	 */
	private final class ClearMenuAction extends AbstractCyAction {

		public ClearMenuAction() {
			super("Clear Menu");
			setPreferredMenu(MENU_CATEGORY);
			insertSeparatorBefore = true;
			setMenuGravity(10001.0f);
		}

		@Override
		public boolean isEnabled() {
			return !currentMenuItems.isEmpty();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final RecentlyOpenedTracker tracker = serviceRegistrar.getService(RecentlyOpenedTracker.class);
			tracker.clear();
			updateMenuItems();
		}
	}
}
