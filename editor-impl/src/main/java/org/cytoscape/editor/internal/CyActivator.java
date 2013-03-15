package org.cytoscape.editor.internal;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;

public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		UndoSupport undoSupportServiceRef = getService(bc,UndoSupport.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc, VisualMappingManager.class);
		CyGroupManager cyGroupManagerServiceRef = getService(bc, CyGroupManager.class);
		CreateNetworkViewTaskFactory createNetworkViewTaskFactoryServiceRef = getService(bc, CreateNetworkViewTaskFactory.class);

		// NetworkView (empty space) context menus
		SIFInterpreterTaskFactory sifInterpreterTaskFactory = new SIFInterpreterTaskFactory(visualMappingManagerServiceRef, cyEventHelperServiceRef);
		Properties sifInterpreterTaskFactoryProps = new Properties();
		sifInterpreterTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		sifInterpreterTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		sifInterpreterTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
		sifInterpreterTaskFactoryProps.setProperty(MENU_GRAVITY, "1.0");
		sifInterpreterTaskFactoryProps.setProperty(IN_MENU_BAR, "false");
		sifInterpreterTaskFactoryProps.setProperty(TITLE, "Edge (and possibly Nodes) using SIF...");
		registerService(bc, sifInterpreterTaskFactory, NetworkViewTaskFactory.class, sifInterpreterTaskFactoryProps);

		NetworkViewLocationTaskFactory networkViewLocationTaskFactory = new AddNodeTaskFactory(cyEventHelperServiceRef, visualMappingManagerServiceRef);
		Properties networkViewLocationTaskFactoryProps = new Properties();
		networkViewLocationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		networkViewLocationTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
		networkViewLocationTaskFactoryProps.setProperty(MENU_GRAVITY, "1.1");
		networkViewLocationTaskFactoryProps.setProperty(TITLE, "Node");
		registerService(bc, networkViewLocationTaskFactory, NetworkViewLocationTaskFactory.class, networkViewLocationTaskFactoryProps);

		// We need a place to hold the objects themselves
		ClipboardManagerImpl clipboardManager = new ClipboardManagerImpl();

		// Copy node
		NetworkViewTaskFactory copyTaskFactory = 
			new CopyTaskFactory(clipboardManager, cyNetworkManagerServiceRef);
		Properties copyTaskFactoryProps = new Properties();
		copyTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		copyTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		copyTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
		copyTaskFactoryProps.setProperty(ACCELERATOR, "cmd c");
		copyTaskFactoryProps.setProperty(TITLE, "Copy");
		copyTaskFactoryProps.setProperty(MENU_GRAVITY, "0.0f");
		registerService(bc, copyTaskFactory, NetworkViewTaskFactory.class, copyTaskFactoryProps);

		// Cut node
		NetworkViewTaskFactory cutTaskFactory = 
			new CutTaskFactory(clipboardManager, undoSupportServiceRef, cyNetworkManagerServiceRef);
		Properties cutTaskFactoryProps = new Properties();
		cutTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		cutTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		cutTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
		cutTaskFactoryProps.setProperty(ACCELERATOR, "cmd x");
		cutTaskFactoryProps.setProperty(MENU_GRAVITY, "0.1f");
		cutTaskFactoryProps.setProperty(TITLE, "Cut");
		registerService(bc, cutTaskFactory, NetworkViewTaskFactory.class, cutTaskFactoryProps);

		// Paste node
		NetworkViewLocationTaskFactory pasteTaskFactory = 
			new PasteTaskFactory(clipboardManager, cyEventHelperServiceRef, 
		                       undoSupportServiceRef, visualMappingManagerServiceRef);
		Properties pasteTaskFactoryProps = new Properties();
		pasteTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		pasteTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		pasteTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
		pasteTaskFactoryProps.setProperty(TITLE, "Paste");
		pasteTaskFactoryProps.setProperty(MENU_GRAVITY, "0.2f");
		pasteTaskFactoryProps.setProperty(ACCELERATOR, "cmd v");
		pasteTaskFactoryProps.setProperty(IN_MENU_BAR, "true");
		registerService(bc, pasteTaskFactory, NetworkViewLocationTaskFactory.class, pasteTaskFactoryProps);

		// At some point, add Paste Special.  Paste special would allow paste node only, paste copy, etc.

		// NodeView context menus
		// Copy node
		NodeViewTaskFactory copyNodeTaskFactory = 
			new CopyNodeTaskFactory(clipboardManager, cyNetworkManagerServiceRef);
		Properties copyNodeTaskFactoryProps = new Properties();
		copyNodeTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		copyNodeTaskFactoryProps.setProperty(PREFERRED_MENU, NODE_EDIT_MENU);
		copyNodeTaskFactoryProps.setProperty(ACCELERATOR, "cmd c");
		copyNodeTaskFactoryProps.setProperty(MENU_GRAVITY, "0.0f");
		copyNodeTaskFactoryProps.setProperty(TITLE, "Copy");
		registerService(bc, copyNodeTaskFactory, NodeViewTaskFactory.class, copyNodeTaskFactoryProps);

		// Cut node
		NodeViewTaskFactory cutNodeTaskFactory = 
			new CutNodeTaskFactory(clipboardManager, undoSupportServiceRef, cyNetworkManagerServiceRef);
		Properties cutNodeTaskFactoryProps = new Properties();
		cutNodeTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		cutNodeTaskFactoryProps.setProperty(PREFERRED_MENU, NODE_EDIT_MENU);
		cutNodeTaskFactoryProps.setProperty(ACCELERATOR, "cmd x");
		cutNodeTaskFactoryProps.setProperty(MENU_GRAVITY, "0.1f");
		cutNodeTaskFactoryProps.setProperty(TITLE, "Cut");
		registerService(bc, cutNodeTaskFactory, NodeViewTaskFactory.class, cutNodeTaskFactoryProps);

		// Rename node
		NodeViewTaskFactory renameNodeTaskFactory = 
			new RenameNodeTaskFactory(undoSupportServiceRef);
		Properties renameNodeTaskFactoryProps = new Properties();
		renameNodeTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		renameNodeTaskFactoryProps.setProperty(PREFERRED_MENU, NODE_EDIT_MENU);
		renameNodeTaskFactoryProps.setProperty(MENU_GRAVITY, "0.2f");
		renameNodeTaskFactoryProps.setProperty(TITLE, "Rename Node");
		registerService(bc, renameNodeTaskFactory, NodeViewTaskFactory.class, renameNodeTaskFactoryProps);

		NodeViewTaskFactory addNestedNetworkTaskFactory = 
			new AddNestedNetworkTaskFactory(cyNetworkManagerServiceRef, visualMappingManagerServiceRef, cyGroupManagerServiceRef);
		Properties addNestedNetworkProps = new Properties();
		addNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		addNestedNetworkProps.setProperty(PREFERRED_MENU, NODE_NESTED_NETWORKS_MENU);
		addNestedNetworkProps.setProperty(MENU_GRAVITY, "0.1f");
		addNestedNetworkProps.setProperty(TITLE, "Add Nested Network");
		registerService(bc, addNestedNetworkTaskFactory, NodeViewTaskFactory.class, addNestedNetworkProps);

		NodeViewTaskFactory deleteNestedNetworkTaskFactory = 
			new DeleteNestedNetworkTaskFactory(cyNetworkManagerServiceRef, visualMappingManagerServiceRef, cyGroupManagerServiceRef);
		Properties deleteNestedNetworkProps = new Properties();
		deleteNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		deleteNestedNetworkProps.setProperty(PREFERRED_MENU, NODE_NESTED_NETWORKS_MENU);
		deleteNestedNetworkProps.setProperty(MENU_GRAVITY, "0.2f");
		deleteNestedNetworkProps.setProperty(TITLE, "Remove Nested Network");
		registerService(bc, deleteNestedNetworkTaskFactory, NodeViewTaskFactory.class, deleteNestedNetworkProps);
		
		NodeViewTaskFactory goToNestedNetworkTaskFactory = new GoToNestedNetworkTaskFactory(cyNetworkManagerServiceRef, cyNetworkViewManagerServiceRef, cyApplicationManagerServiceRef, createNetworkViewTaskFactoryServiceRef);
		Properties goToNestedNetworkProps = new Properties();
		goToNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		goToNestedNetworkProps.setProperty(PREFERRED_MENU, NODE_NESTED_NETWORKS_MENU);
		goToNestedNetworkProps.setProperty(MENU_GRAVITY, "0.3f");
		goToNestedNetworkProps.setProperty(TITLE, "Go to Nested Network");
		registerService(bc, goToNestedNetworkTaskFactory, NodeViewTaskFactory.class, goToNestedNetworkProps);

		// EdgeView context menus
		// Copy node
		EdgeViewTaskFactory copyEdgeTaskFactory = 
			new CopyEdgeTaskFactory(clipboardManager, cyNetworkManagerServiceRef);
		Properties copyEdgeTaskFactoryProps = new Properties();
		copyEdgeTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		copyEdgeTaskFactoryProps.setProperty(PREFERRED_MENU, EDGE_EDIT_MENU);
		copyEdgeTaskFactoryProps.setProperty(ACCELERATOR, "cmd c");
		copyEdgeTaskFactoryProps.setProperty(MENU_GRAVITY, "0.0f");
		copyEdgeTaskFactoryProps.setProperty(TITLE, "Copy");
		registerService(bc, copyEdgeTaskFactory, EdgeViewTaskFactory.class, copyEdgeTaskFactoryProps);

		// Cut edge
		EdgeViewTaskFactory cutEdgeTaskFactory = 
			new CutEdgeTaskFactory(clipboardManager, undoSupportServiceRef, cyNetworkManagerServiceRef);
		Properties cutEdgeTaskFactoryProps = new Properties();
		cutEdgeTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		cutEdgeTaskFactoryProps.setProperty(PREFERRED_MENU, EDGE_EDIT_MENU);
		cutEdgeTaskFactoryProps.setProperty(ACCELERATOR, "cmd x");
		cutEdgeTaskFactoryProps.setProperty(MENU_GRAVITY, "0.1f");
		cutEdgeTaskFactoryProps.setProperty(TITLE, "Cut");
		registerService(bc, cutEdgeTaskFactory, EdgeViewTaskFactory.class, cutEdgeTaskFactoryProps);
	}
}
