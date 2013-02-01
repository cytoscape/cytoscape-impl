package org.cytoscape.internal.view;

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



import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.task.CytoPanelTaskFactoryTunableAction;
import org.cytoscape.internal.task.TaskFactoryTunableAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;

import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;


/**
 * Creates the menu and tool bars for a Cytoscape window object. It
 * also provides access to individual menus and items.<BR>
 * <p>
 * AddAction takes one more optional argument to specify index. App
 * writers can use this function to specify the location of the menu item.
 * </p>
 */
public class CytoscapeMenuPopulator {
	final private CySwingApplication app;
	final private PanelTaskManager panelTaskManager;
	final private DialogTaskManager dialogTaskManager;
	final private CyApplicationManager appManager;
	final private CyServiceRegistrar registrar;
	final private DynamicTaskFactoryProvisioner factoryProvisioner;

	final private Map<TaskFactory, CyAction> taskMap;
	final private Map<Object, TaskFactory> provisionerMap;
	
	private final CyNetworkViewManager networkViewManager;


	/**
	 * Creates a new CytoscapeMenus object. This will construct the basic bar objects, 
	 * but won't fill them with menu items and associated action listeners.
	 */
	public CytoscapeMenuPopulator(final CySwingApplication app, final DialogTaskManager dialogTaskManager,
	                  final PanelTaskManager panelTaskManager,
				      final CyApplicationManager appManager, final CyNetworkViewManager networkViewManager, final CyServiceRegistrar registrar,
				      DynamicTaskFactoryProvisioner factoryProvisioner)
	{
		this.app = app;
		this.networkViewManager = networkViewManager;
		this.dialogTaskManager = dialogTaskManager;
		this.panelTaskManager = panelTaskManager;
		this.appManager = appManager;
		this.registrar = registrar;
		this.factoryProvisioner = factoryProvisioner;

		taskMap = new HashMap<TaskFactory,CyAction>();
		provisionerMap = new IdentityHashMap<Object, TaskFactory>();
	}

	public void addTaskFactory(TaskFactory factory, Map props) {
		String pref = (String)(props.get("preferredTaskManager"));
		if (pref != null && pref.equals("panel")) {
			addAction(new CytoPanelTaskFactoryTunableAction(factory, null, panelTaskManager, 
			                                                props, appManager, networkViewManager, registrar), 
			                                                factory);
		} else {
			addFactory(factory, props);
		}
	}

	public void removeTaskFactory(TaskFactory factory, Map props) {
		removeFactory(factory, props);
	}

	public void addNetworkTaskFactory(NetworkTaskFactory factory, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}

	public void removeNetworkTaskFactory(NetworkTaskFactory factory, Map props) {
		removeFactory(provisionerMap.get(factory), props);
	}

	public void addNetworkViewTaskFactory(NetworkViewTaskFactory factory, Map props) {
		// Check to make sure this is supposed to be in the menus
		if (props.containsKey(IN_MENU_BAR) && !Boolean.parseBoolean(props.get(IN_MENU_BAR).toString()))
			return;
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory factory, Map props) {
		removeFactory(provisionerMap.get(factory), props);
	}

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}

	public void removeNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory, Map props) {
		removeFactory(provisionerMap.get(factory), props);
	}
	
	public void addNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}

	public void removeNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map props) {
		removeFactory(provisionerMap.get(factory), props);
	}
	
	public void addTableTaskFactory(TableTaskFactory factory, Map props) {
		TaskFactory provisioner = factoryProvisioner.createFor(factory);
		provisionerMap.put(factory, provisioner);
		addFactory(provisioner, props);
	}
	
	public void removeTableTaskFactory(TableTaskFactory factory, Map props) {
		removeFactory(provisionerMap.get(factory), props);
	}
	
	private void addFactory(TaskFactory factory, Map props) {
		CyAction action;
		if ( props.containsKey("enableFor") )
			action = new TaskFactoryTunableAction(dialogTaskManager, factory, props, appManager, networkViewManager);
		else	
			action = new TaskFactoryTunableAction(dialogTaskManager, factory, props);

		addAction(action,factory);
	}

	private void addAction(CyAction action, TaskFactory factory) {
		taskMap.put(factory,action);
		app.addAction(action);
	}

	private void removeFactory(TaskFactory factory, Map props) {
		final CyAction action = taskMap.remove(factory);
		if (action != null) {
			if (action.isInMenuBar())
				app.removeAction(action);

			if (action.isInToolBar())
				app.removeAction(action);
		}
	}
}
