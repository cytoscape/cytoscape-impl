package org.cytoscape.internal.task;

import static org.cytoscape.work.ServiceProperties.LARGE_ICON_ID;
import static org.cytoscape.work.ServiceProperties.SMALL_ICON_ID;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.event.MenuEvent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.Togglable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
public class TaskFactoryTunableAction extends AbstractCyAction implements PopupMenuListener {

	private final static Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private final TaskFactory factory;
	private final CyServiceRegistrar serviceRegistrar;

	/**
	 * This constructor forces the action to use the TaskFactory.isReady() method
	 * to determine whether the action should be enabled.
	 */
	public TaskFactoryTunableAction(CyServiceRegistrar serviceRegistrar, TaskFactory factory,
			Map<String, String> props) {
		super(props, factory);
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
		config();
	}

	/**
	 * This constructor forces the action to use the {@link ServiceProperties#ENABLE_FOR} property in props
	 * to determine whether the action should be enabled.
	 */
	public TaskFactoryTunableAction(TaskFactory factory, Map<String, String> props,
			CyServiceRegistrar serviceRegistrar) {
		super(
				props,
				serviceRegistrar.getService(CyApplicationManager.class),
				serviceRegistrar.getService(CyNetworkViewManager.class),
				factory
		);
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
		config();
	}
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		logger.debug("About to execute task from factory: " + factory.toString());

		// execute the task(s) in a separate thread
		var taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		taskManager.execute(factory.createTaskIterator());
	}
	
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {
		if (factory instanceof Togglable)
			putValue(SELECTED_KEY, factory.isOn());
	}
	
	@Override
	public void menuSelected(MenuEvent evt) {
		super.menuSelected(evt);
		
		if (factory instanceof Togglable)
			putValue(SELECTED_KEY, factory.isOn());
	}
	
	private void config() {
		// Togglable
		useCheckBoxMenuItem = useToggleButton = (factory instanceof Togglable);
		
		// Icons
		var iconManager = serviceRegistrar.getService(IconManager.class);
		var largeIconId = configurationProperties.get(LARGE_ICON_ID);

		if (largeIconId != null && !largeIconId.trim().isEmpty()) {
			// Check if the icon is really registered
			var icon = iconManager.getIcon(largeIconId);
			
			if (icon != null)
				putValue(LARGE_ICON_KEY, icon);
		}
		
		var smallIconId = configurationProperties.get(SMALL_ICON_ID);
		
		if (smallIconId != null && !smallIconId.trim().isEmpty()) {
			// Check if the icon is really registered
			var icon = iconManager.getIcon(smallIconId);
			
			if (icon != null)
				putValue(SMALL_ICON, icon);
		}
	}
}
