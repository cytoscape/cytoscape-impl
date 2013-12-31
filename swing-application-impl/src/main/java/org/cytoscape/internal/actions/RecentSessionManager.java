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
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.internal.task.OpenRecentSessionTaskFactory;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update menu
 * 
 */
public class RecentSessionManager implements SessionLoadedListener, CyShutdownListener {
	
	private static final Logger logger = LoggerFactory.getLogger(RecentSessionManager.class);
	
	private static final String MENU_CATEGORY = "File.Recent Session";

	private final RecentlyOpenedTracker tracker;
	private final CyServiceRegistrar registrar;
	private final CySessionManager sessionManager;
	private final CySessionReaderManager readerManager;
	private final CyApplicationManager appManager;
	private final CyNetworkManager netManager;
	private final CyTableManager tableManager;
	private final CyNetworkTableManager netTableManager;
	private final CyGroupManager grManager;
	private final CyEventHelper eventHelper;

	private final Set<OpenRecentSessionTaskFactory> currentMenuItems;
	
	private final DummyAction factory;

	public RecentSessionManager(final RecentlyOpenedTracker tracker,
								final CyServiceRegistrar registrar,
								final CySessionManager sessionManager,
								final CySessionReaderManager readerManager,
								final CyApplicationManager appManager,
								final CyNetworkManager netManager,
								final CyTableManager tableManager,
								final CyNetworkTableManager netTableManager,
								final CyGroupManager grManager,
								final CyEventHelper eventHelper) {
		this.tracker = tracker;
		this.registrar = registrar;
		this.sessionManager = sessionManager;
		this.readerManager = readerManager;
		this.appManager = appManager;
		this.netManager = netManager;
		this.tableManager = tableManager;
		this.netTableManager = netTableManager;
		this.grManager = grManager;
		this.eventHelper = eventHelper;
		
		this.currentMenuItems = new HashSet<OpenRecentSessionTaskFactory>();

		factory = new DummyAction();
		
		updateMenuItems();
	}

	private void updateMenuItems() {
		// If there is no recent items, add dummy menu.
		if(tracker.getRecentlyOpenedURLs().size() == 0) {
			registrar.registerService(factory, CyAction.class, new Properties());
			return;
		}
			
		// Unregister services
		registrar.unregisterService(factory, CyAction.class);
		for (final OpenRecentSessionTaskFactory currentItem : currentMenuItems)
			registrar.unregisterAllServices(currentItem);

		currentMenuItems.clear();

		final List<URL> urls = tracker.getRecentlyOpenedURLs();

		for (final URL url : urls) {
			final Properties prop = new Properties();
			prop.put(ServiceProperties.PREFERRED_MENU, MENU_CATEGORY);
			prop.put(ServiceProperties.TITLE, url.getFile());
			prop.put(ServiceProperties.MENU_GRAVITY, "6.0");
			final OpenRecentSessionTaskFactory factory = new OpenRecentSessionTaskFactory(sessionManager, readerManager,
					appManager, netManager, tableManager, netTableManager, grManager, tracker, url, eventHelper);
			registrar.registerService(factory, TaskFactory.class, prop);

			this.currentMenuItems.add(factory);
		}

	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		updateMenuItems();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateMenuItems();
			}
		});
	}
	
	
	/**
	 * Dummy action to add menu item when no entry is available.
	 */
	private final class DummyAction extends AbstractCyAction {

		private static final long serialVersionUID = 4904285068314580548L;

		public DummyAction() {
			super("(No recent session files)");
			setPreferredMenu(MENU_CATEGORY);
			setMenuGravity(6.0f);
			this.setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {}
	}

	@Override
	public void handleEvent(CyShutdownEvent e) {
		logger.info("Saving recently used session file list...");
		try {
			tracker.writeOut();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Could not save recently opened session file list.", ex);
		}
	}
}
