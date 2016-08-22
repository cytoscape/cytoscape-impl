package org.cytoscape.editor.internal;

import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.EDGE_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NETWORK_ADD_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_NESTED_NETWORKS_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

		{
			// NetworkView (empty space) context menus
			SIFInterpreterTaskFactory factory = new SIFInterpreterTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, "networkAndView");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(IN_MENU_BAR, "false");
			props.setProperty(TITLE, "Edge (and possibly Nodes) using SIF...");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			NetworkViewLocationTaskFactory factory = new AddNodeTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
			props.setProperty(MENU_GRAVITY, "1.1");
			props.setProperty(TITLE, "Node");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		
		// We need a place to hold the objects themselves
		ClipboardManagerImpl clipboardManager = new ClipboardManagerImpl(serviceRegistrar);

		{
			// Copy node
			NetworkViewTaskFactory factory = new CopyTaskFactory(clipboardManager);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, "networkAndView");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(ACCELERATOR, "cmd c");
			props.setProperty(TITLE, "Copy");
			props.setProperty(MENU_GRAVITY, "0.0f");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			// Cut node
			NetworkViewTaskFactory factory = new CutTaskFactory(clipboardManager, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, "networkAndView");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(ACCELERATOR, "cmd x");
			props.setProperty(MENU_GRAVITY, "0.1f");
			props.setProperty(TITLE, "Cut");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			// Paste node
			NetworkViewLocationTaskFactory pasteTaskFactory = new PasteTaskFactory(clipboardManager, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, "networkAndView");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Paste");
			props.setProperty(MENU_GRAVITY, "0.2f");
			props.setProperty(ACCELERATOR, "cmd v");
			props.setProperty(IN_MENU_BAR, "true");
			registerService(bc, pasteTaskFactory, NetworkViewLocationTaskFactory.class, props);
		}

		// At some point, add Paste Special.  Paste special would allow paste node only, paste copy, etc.

		// NodeView context menus
		{
			// Copy node
			NodeViewTaskFactory factory = new CopyNodeTaskFactory(clipboardManager);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NODE_EDIT_MENU);
			props.setProperty(ACCELERATOR, "cmd c");
			props.setProperty(MENU_GRAVITY, "0.0f");
			props.setProperty(TITLE, "Copy");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
		}
		{
			// Cut node
			NodeViewTaskFactory factory = new CutNodeTaskFactory(clipboardManager, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NODE_EDIT_MENU);
			props.setProperty(ACCELERATOR, "cmd x");
			props.setProperty(MENU_GRAVITY, "0.1f");
			props.setProperty(TITLE, "Cut");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
		}
		{
			// Rename node
			NodeViewTaskFactory factory = new RenameNodeTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NODE_EDIT_MENU);
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			props.setProperty(MENU_GRAVITY, "0.2f");
			props.setProperty(TITLE, "Rename Node");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
		}

		{
			NodeViewTaskFactory factory = new AddNestedNetworkTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NODE_NESTED_NETWORKS_MENU);
			props.setProperty(MENU_GRAVITY, "0.1f");
			props.setProperty(TITLE, "Add Nested Network");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
		}
		{
			NodeViewTaskFactory factory = new DeleteNestedNetworkTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NODE_NESTED_NETWORKS_MENU);
			props.setProperty(MENU_GRAVITY, "0.2f");
			props.setProperty(TITLE, "Remove Nested Network");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
		}
		{
			NodeViewTaskFactory factory = new GoToNestedNetworkTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NODE_NESTED_NETWORKS_MENU);
			props.setProperty(MENU_GRAVITY, "0.3f");
			props.setProperty(TITLE, "Go to Nested Network");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
		}

		// EdgeView context menus
		{
			// Copy node
			EdgeViewTaskFactory factory = new CopyEdgeTaskFactory(clipboardManager);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, EDGE_EDIT_MENU);
			props.setProperty(ACCELERATOR, "cmd c");
			props.setProperty(MENU_GRAVITY, "0.0f");
			props.setProperty(TITLE, "Copy");
			registerService(bc, factory, EdgeViewTaskFactory.class, props);
		}
		{
			// Cut edge
			EdgeViewTaskFactory factory = new CutEdgeTaskFactory(clipboardManager, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, EDGE_EDIT_MENU);
			props.setProperty(ACCELERATOR, "cmd x");
			props.setProperty(MENU_GRAVITY, "0.1f");
			props.setProperty(TITLE, "Cut");
			registerService(bc, factory, EdgeViewTaskFactory.class, props);
		}
	}
}
