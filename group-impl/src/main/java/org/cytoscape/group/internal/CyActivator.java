package org.cytoscape.group.internal;

/*
 * #%L
 * Cytoscape Groups Impl (group-impl)
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

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.group.events.GroupAddedListener;
import org.cytoscape.group.events.GroupAboutToCollapseListener;
import org.cytoscape.group.events.GroupCollapsedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.events.ViewChangedListener;
import static org.cytoscape.work.ServiceProperties.*;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import org.cytoscape.group.internal.data.CyGroupAggregationManagerImpl;
import org.cytoscape.group.internal.data.CyGroupSettingsImpl;
import org.cytoscape.group.internal.data.CyGroupSettingsTaskFactory;
import org.cytoscape.group.internal.data.CyGroupNodeSettingsTaskFactory;

import org.cytoscape.group.internal.view.NodeChangeListener;
import org.cytoscape.group.internal.view.GroupViewCollapseHandler;
import org.cytoscape.group.internal.view.GroupViewDoubleClickListener;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		CyEventHelper cyEventHelper = getService(bc,CyEventHelper.class);

		LockedVisualPropertiesManager lockedVisualPropertiesManager =
				new LockedVisualPropertiesManager(cyServiceRegistrarServiceRef);
		
		CyGroupManagerImpl cyGroupManager = new CyGroupManagerImpl(cyServiceRegistrarServiceRef, cyEventHelper);
		CyGroupFactoryImpl cyGroupFactory = new CyGroupFactoryImpl(cyGroupManager, lockedVisualPropertiesManager,
		                                                           cyEventHelper);
		registerService(bc,cyGroupManager,CyGroupManager.class, new Properties());
		registerService(bc,cyGroupFactory,CyGroupFactory.class, new Properties());

		// Create the aggregation manager
		CyGroupAggregationManager cyAggMgr = 
			new CyGroupAggregationManagerImpl(cyGroupManager);
    registerService(bc,cyAggMgr,CyGroupAggregationManager.class, new Properties());
		
		GroupIO groupIO = new GroupIO(cyGroupManager, lockedVisualPropertiesManager);
		registerAllServices(bc, groupIO, new Properties());

		// Get our Settings object
		CyGroupSettingsImpl cyGroupSettings = 
			new CyGroupSettingsImpl(cyGroupManager, cyAggMgr);

		// Register our settings menu
    CyGroupSettingsTaskFactory settingsFactory = 
			new CyGroupSettingsTaskFactory(cyGroupManager, cyAggMgr,
			                               cyGroupSettings);

    Properties settingsProps = new Properties();
    settingsProps.setProperty(ID,"settingsFactory");
    settingsProps.setProperty(PREFERRED_MENU,"Edit.Preferences");
    settingsProps.setProperty(TITLE, "Group Preferences...");
    settingsProps.setProperty(MENU_GRAVITY,"4.0");
    settingsProps.setProperty(TOOL_BAR_GRAVITY,"4");
    settingsProps.setProperty(IN_TOOL_BAR,"false");
    registerService(bc,settingsFactory,TaskFactory.class, settingsProps);

		// Now register our node-specific settings menu
    CyGroupNodeSettingsTaskFactory nodeSettingsFactory = 
			new CyGroupNodeSettingsTaskFactory(cyGroupManager,
			                                   cyAggMgr, 
			                                   cyGroupSettings);
    settingsProps = new Properties();
    settingsProps.setProperty(ID,"groupNodeSettingsFactory");
    settingsProps.setProperty(PREFERRED_MENU,NODE_PREFERENCES_MENU);
    settingsProps.setProperty(MENU_GRAVITY, "-1");
    settingsProps.setProperty(TITLE, "Group Preferences...");
		settingsProps.setProperty(PREFERRED_ACTION, "NEW");
		settingsProps.setProperty(COMMAND, "group-node-settings");
		settingsProps.setProperty(COMMAND_NAMESPACE, "network-view");
    registerService(bc,nodeSettingsFactory,
		                NodeViewTaskFactory.class, settingsProps);

		// Make the settings available to consumers
    registerService(bc,cyGroupSettings, 
		                GroupAddedListener.class, new Properties());
    registerService(bc,cyGroupSettings, 
		                CyGroupSettingsImpl.class, new Properties());

		// Set up listener for node movement
		NodeChangeListener nodeChangeListener = new NodeChangeListener(cyGroupManager, cyEventHelper);

		GroupViewCollapseHandler gvcHandler = 
			new GroupViewCollapseHandler(cyGroupManager, cyGroupSettings,
			                             nodeChangeListener, cyEventHelper);

		registerService(bc,gvcHandler,GroupAboutToCollapseListener.class, new Properties());
		registerService(bc,gvcHandler,GroupCollapsedListener.class, new Properties());
		registerService(bc,nodeChangeListener,ViewChangedListener.class, new Properties());

		// Listen for double-click
		GroupViewDoubleClickListener gvsListener =
			new GroupViewDoubleClickListener(cyGroupManager, cyGroupSettings);

		Properties doubleClickProperties = new Properties();
		doubleClickProperties.setProperty(PREFERRED_ACTION, "OPEN");
		doubleClickProperties.setProperty(TITLE, "Expand/Collapse Group");
		registerService(bc,gvsListener,NodeViewTaskFactory.class, doubleClickProperties);

	}
}

