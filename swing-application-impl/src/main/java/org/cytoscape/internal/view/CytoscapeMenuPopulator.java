/*
 File: CytoscapeMenus.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.internal.view;



import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.task.CytoPanelTaskFactoryTunableAction;
import org.cytoscape.internal.task.NetworkCollectionTaskFactoryTunableAction;
import org.cytoscape.internal.task.NetworkTaskFactoryTunableAction;
import org.cytoscape.internal.task.NetworkViewCollectionTaskFactoryTunableAction;
import org.cytoscape.internal.task.NetworkViewTaskFactoryTunableAction;
import org.cytoscape.internal.task.TableTaskFactoryTunableAction;
import org.cytoscape.internal.task.TaskFactoryTunableAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITaskManager;


/**
 * Creates the menu and tool bars for a Cytoscape window object. It
 * also provides access to individual menus and items.<BR>
 * <p>
 * AddAction takes one more optional argument to specify index. Plugin
 * writers can use this function to specify the location of the menu item.
 * </p>
 */
public class CytoscapeMenuPopulator {
	final private CySwingApplication app;
	final private GUITaskManager taskManager;
	final private CyApplicationManager appManager;
	final private CyServiceRegistrar registrar;

	final private Map<TaskFactory, CyAction> taskMap;


	/**
	 * Creates a new CytoscapeMenus object. This will construct the basic bar objects, 
	 * but won't fill them with menu items and associated action listeners.
	 */
	public CytoscapeMenuPopulator(final CySwingApplication app, final GUITaskManager taskManager,
				      final CyApplicationManager appManager, final CyServiceRegistrar registrar)
	{
		this.app = app;
		this.taskManager = taskManager;
		this.appManager = appManager;
		this.registrar = registrar;

		taskMap = new HashMap<TaskFactory,CyAction>();
	}

	public void addTaskFactory(TaskFactory factory, Map props) {
		if (taskManager.hasTunables(factory))
			addFactory(new CytoPanelTaskFactoryTunableAction(factory, taskManager, props, appManager, registrar), factory, props);
		else
			addFactory(new TaskFactoryTunableAction<TaskFactory>(taskManager, factory, props, appManager), factory, props);
	}

	public void removeTaskFactory(TaskFactory factory, Map props) {
		removeFactory(factory, props);
	}

	public void addNetworkTaskFactory(NetworkTaskFactory factory, Map props) {
		addFactory(new NetworkTaskFactoryTunableAction(taskManager, factory, props, appManager), factory, props);
	}

	public void removeNetworkTaskFactory(NetworkTaskFactory factory, Map props) {
		removeFactory(factory, props);
	}

	public void addNetworkViewTaskFactory(NetworkViewTaskFactory factory, Map props) {
		addFactory(new NetworkViewTaskFactoryTunableAction(taskManager, factory, props, appManager), factory, props);
	}

	public void removeNetworkViewTaskFactory(NetworkViewTaskFactory factory, Map props) {
		removeFactory(factory, props);
	}

	public void addNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory, Map props) {
		addFactory(new NetworkViewCollectionTaskFactoryTunableAction(taskManager, factory, props, appManager), factory, props);
	}

	public void removeNetworkViewCollectionTaskFactory(NetworkViewCollectionTaskFactory factory, Map props) {
		removeFactory(factory, props);
	}
	
	public void addNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map props) {
		addFactory(new NetworkCollectionTaskFactoryTunableAction(taskManager, factory, props, appManager), factory, props);
	}

	public void removeNetworkCollectionTaskFactory(NetworkCollectionTaskFactory factory, Map props) {
		removeFactory(factory, props);
	}
	
	public void addTableTaskFactory(TableTaskFactory factory, Map props) {
		addFactory(new TableTaskFactoryTunableAction(taskManager, factory, props, appManager), factory, props);
	}
	
	public void removeTableTaskFactory(TableTaskFactory factory, Map props) {
		removeFactory(factory, props);
	}
	
	private <F extends TaskFactory> void addFactory(CyAction action, F factory, Map props) {
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
