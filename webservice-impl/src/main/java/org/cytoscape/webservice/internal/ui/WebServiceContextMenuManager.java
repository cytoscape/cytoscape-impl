package org.cytoscape.webservice.internal.ui;

/*
 * #%L
 * Cytoscape Webservice Impl (webservice-impl)
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

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.View;


/**
 * Context menu for web service clients.
 *
 */
public class WebServiceContextMenuManager {
		
	private JMenu nodeRootMenu;
	private JMenu edgeRootMenu;

	/**
	 * Creates a new WebServiceContextMenu object.
	 */
	public WebServiceContextMenuManager() {
		nodeRootMenu = new JMenu("Use Web Services");
		edgeRootMenu = new JMenu("Use Web Services");
	}

	/**
	 *  Add this menu to the node context menu.
	 *
	 * @param nodeView DOCUMENT ME!
	 * @param menu DOCUMENT ME!
	 */
	public void addNodeContextMenuItems(View<CyNode> nodeView, JPopupMenu menu) {
		addMenu(nodeView, menu);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param edgeView DOCUMENT ME!
	 * @param menu DOCUMENT ME!
	 */
	public void addEdgeContextMenuItems(View<CyEdge> edgeView, JPopupMenu menu) {
		addMenu(edgeView, menu);
	}

	private void addMenu(View<? extends CyIdentifiable> view, JPopupMenu menu) {
		if (menu == null)
			menu = new JPopupMenu();
		else {
			// Clean up menu
			nodeRootMenu.removeAll();
			edgeRootMenu.removeAll();
		}

//		List<JMenuItem> context = null;
//		final List<WebServiceClient<?>> clients = WebServiceClientManager.getAllClients();
//
//		for (WebServiceClient<?> client : clients) {
//			if (client instanceof WebServiceClientGUI) {
//				if (view.getModel() instanceof CyNode)
//					context = ((WebServiceClientGUI) client).getNodeContextMenuItems((NodeView) view);
//				else if (view.getModel() instanceof CyEdge)
//					context = ((WebServiceClientGUI) client).getEdgeContextMenuItems((EdgeView) view);
//
//				if (context != null) {
//					JMenu cMenu = new JMenu(client.getDisplayName());
//
//					for (JMenuItem menuItem : context) {
//						cMenu.add(menuItem);
//					}
//
//					nodeRootMenu.add(cMenu);
//					clientMap.put(client.getDisplayName(), client.getClientID());
//				}
//			}
//		}
//
//		if(view instanceof NodeView) {
//			menu.add(this.nodeRootMenu);
//			if(nodeRootMenu.getItemCount() == 0)
//				nodeRootMenu.setEnabled(false);
//		} else {
//			menu.add(this.edgeRootMenu);
//			if(edgeRootMenu.getItemCount() == 0)
//				edgeRootMenu.setEnabled(false);
//		}
	}
}
