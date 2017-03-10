package org.cytoscape.group.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NODE_PREFERENCES_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.util.Properties;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.CyGroupSettingsManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.events.GroupAboutToBeDestroyedListener;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupAddedListener;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.group.internal.data.CyGroupAggregationManagerImpl;
import org.cytoscape.group.internal.data.CyGroupNodeSettingsTaskFactory;
import org.cytoscape.group.internal.data.CyGroupSettingsImpl;
import org.cytoscape.group.internal.data.CyGroupSettingsTaskFactory;
import org.cytoscape.group.internal.data.GroupDataCollapseHandler;
import org.cytoscape.group.internal.data.GroupViewTypeChangedListener;
import org.cytoscape.group.internal.view.GroupViewCollapseHandler;
import org.cytoscape.group.internal.view.GroupViewDoubleClickListener;
import org.cytoscape.group.internal.view.NodeChangeListener;
import org.cytoscape.model.events.AboutToRemoveEdgesListener;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		LockedVisualPropertiesManager lockedVisualPropertiesManager = new LockedVisualPropertiesManager(
				serviceRegistrar);

		CyGroupManagerImpl cyGroupManager = new CyGroupManagerImpl(serviceRegistrar);
		CyGroupFactoryImpl cyGroupFactory = new CyGroupFactoryImpl(cyGroupManager, lockedVisualPropertiesManager);
		registerService(bc, cyGroupManager, CyGroupManager.class, new Properties());
		registerService(bc, cyGroupManager, AddedEdgesListener.class, new Properties());
		registerService(bc, cyGroupManager, AboutToRemoveEdgesListener.class, new Properties());
		registerService(bc, cyGroupFactory, CyGroupFactory.class, new Properties());

		// Create the aggregation manager
		CyGroupAggregationManagerImpl cyAggMgr = new CyGroupAggregationManagerImpl(cyGroupManager);
		registerService(bc, cyAggMgr, CyGroupAggregationManager.class, new Properties());

		// Get our Settings object
		CyGroupSettingsImpl cyGroupSettings = new CyGroupSettingsImpl(cyGroupManager, cyAggMgr, serviceRegistrar);

		GroupIO groupIO = new GroupIO(cyGroupManager, lockedVisualPropertiesManager, cyGroupSettings);
		registerAllServices(bc, groupIO, new Properties());

		// Register our settings menu
		CyGroupSettingsTaskFactory settingsFactory = new CyGroupSettingsTaskFactory(cyGroupManager, cyAggMgr,
				cyGroupSettings);

		Properties settingsProps = new Properties();
		settingsProps.setProperty(ID, "settingsFactory");
		settingsProps.setProperty(PREFERRED_MENU, "Edit.Preferences");
		settingsProps.setProperty(TITLE, "Group Preferences...");
		settingsProps.setProperty(MENU_GRAVITY, "4.0");
		settingsProps.setProperty(TOOL_BAR_GRAVITY, "4");
		settingsProps.setProperty(IN_TOOL_BAR, "false");
		registerService(bc, settingsFactory, TaskFactory.class, settingsProps);

		// Now register our node-specific settings menu
		CyGroupNodeSettingsTaskFactory nodeSettingsFactory = new CyGroupNodeSettingsTaskFactory(cyGroupManager,
				cyAggMgr, cyGroupSettings);
		settingsProps = new Properties();
		settingsProps.setProperty(ID, "groupNodeSettingsFactory");
		settingsProps.setProperty(PREFERRED_MENU, NODE_PREFERENCES_MENU);
		settingsProps.setProperty(MENU_GRAVITY, "-1");
		settingsProps.setProperty(TITLE, "Group Preferences...");
		settingsProps.setProperty(PREFERRED_ACTION, "NEW");
		settingsProps.setProperty(COMMAND, "group-node-settings");
		settingsProps.setProperty(COMMAND_NAMESPACE, "network-view");
		registerService(bc, nodeSettingsFactory, NodeViewTaskFactory.class, settingsProps);

		// Make the settings available to consumers
		registerService(bc, cyGroupSettings, GroupAddedListener.class, new Properties());
		registerService(bc, cyGroupSettings, NetworkAddedListener.class, new Properties());
		registerService(bc, cyGroupSettings, CyGroupSettingsManager.class, new Properties());
		registerService(bc, cyGroupSettings, PropertyUpdatedListener.class, new Properties());

		// Set up listener for node movement
		NodeChangeListener nodeChangeListener = new NodeChangeListener(cyGroupManager, cyGroupSettings);
		registerService(bc, nodeChangeListener, ViewChangedListener.class, new Properties());

		GroupViewCollapseHandler gvcHandler = new GroupViewCollapseHandler(cyGroupManager, cyGroupSettings,
				nodeChangeListener);

		registerService(bc, gvcHandler, GroupAboutToCollapseListener.class, new Properties());
		registerService(bc, gvcHandler, GroupAboutToBeDestroyedListener.class, new Properties());
		registerService(bc, gvcHandler, GroupCollapsedListener.class, new Properties());
		registerService(bc, gvcHandler, SessionLoadedListener.class, new Properties());
		registerService(bc, gvcHandler, GroupAddedListener.class, new Properties());
		registerService(bc, gvcHandler, GroupViewTypeChangedListener.class, new Properties());

		GroupDataCollapseHandler gdcHandler = new GroupDataCollapseHandler(cyGroupManager, cyGroupSettings);
		registerService(bc, gdcHandler, GroupAboutToCollapseListener.class, new Properties());

		// Listen for double-click
		GroupViewDoubleClickListener gvsListener = new GroupViewDoubleClickListener(cyGroupManager, cyGroupSettings);

		Properties doubleClickProperties = new Properties();
		doubleClickProperties.setProperty(PREFERRED_ACTION, "OPEN");
		doubleClickProperties.setProperty(TITLE, "Expand/Collapse Group");
		registerService(bc, gvsListener, NodeViewTaskFactory.class, doubleClickProperties);
	}
}
