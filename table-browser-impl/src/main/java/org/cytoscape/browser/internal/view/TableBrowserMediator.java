package org.cytoscape.browser.internal.view;

import static org.cytoscape.browser.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.Component;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

public class TableBrowserMediator implements SetCurrentNetworkListener, CytoPanelComponentSelectedListener {

	private final DefaultTableBrowser nodeTableBrowser;
	private final DefaultTableBrowser edgeTableBrowser;
	private final DefaultTableBrowser networkTableBrowser;
	private final GlobalTableBrowser globalTableBrowser;
	
	private final CyServiceRegistrar serviceRegistrar;

	public TableBrowserMediator(
			DefaultTableBrowser nodeTableBrowser,
			DefaultTableBrowser edgeTableBrowser,
			DefaultTableBrowser networkTableBrowser,
			GlobalTableBrowser globalTableBrowser,
			CyServiceRegistrar serviceRegistrar
	) {
		this.nodeTableBrowser = nodeTableBrowser;
		this.edgeTableBrowser = edgeTableBrowser;
		this.networkTableBrowser = networkTableBrowser;
		this.globalTableBrowser = globalTableBrowser;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent evt) {
		CyNetwork network = evt.getNetwork();
		
		invokeOnEDTAndWait(() -> {
			// Update UI
			nodeTableBrowser.update(network);
			edgeTableBrowser.update(network);
			networkTableBrowser.update(network);
			
			// Get the new current table
			CytoPanel cytoPanel = serviceRegistrar.getService(CySwingApplication.class).getCytoPanel(CytoPanelName.SOUTH);
			Component comp = cytoPanel.getSelectedComponent();
			CyTable table = null;
			
			if (nodeTableBrowser.getComponent() == comp)
				table = nodeTableBrowser.getCurrentTable();
			else if (edgeTableBrowser.getComponent() == comp)
				table = edgeTableBrowser.getCurrentTable();
			else if (networkTableBrowser.getComponent() == comp)
				table = networkTableBrowser.getCurrentTable();
			else if (globalTableBrowser.getComponent() == comp)
				table = globalTableBrowser.getCurrentTable();
			
			// Update the CyApplicationManager
			if (table == null || table.isPublic())
				serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(table);
		});
	}
	
	@Override
	public void handleEvent(CytoPanelComponentSelectedEvent evt) {
		CytoPanel cytoPanel = evt.getCytoPanel();
		int idx = evt.getSelectedIndex();
		
		if (cytoPanel.getCytoPanelName() != CytoPanelName.SOUTH || idx < 0 || idx >= cytoPanel.getCytoPanelComponentCount())
			return;
		
		Component comp = cytoPanel.getComponentAt(idx);
			
		if (comp == null)
			return;
		
		CyTable table = null;
		
		if (comp.equals(nodeTableBrowser.getComponent()))
			table = nodeTableBrowser.getCurrentTable();
		else if (comp.equals(edgeTableBrowser.getComponent()))
			table = edgeTableBrowser.getCurrentTable();
		else if (comp.equals(networkTableBrowser.getComponent()))
			table = networkTableBrowser.getCurrentTable();
		else if (comp.equals(globalTableBrowser.getComponent()))
			table = globalTableBrowser.getCurrentTable();
		
		if (table == null || table.isPublic())
			serviceRegistrar.getService(CyApplicationManager.class).setCurrentTable(table);
	}
	
	public void hideColumn(CyColumn column) {
		CyTable table = column.getTable();
		
		invokeOnEDTAndWait(() -> {
			TableRenderer browserTable = getTableRenderer(table);
			if (browserTable != null)
				browserTable.setColumnVisible(column.getName(), false);
		});
	}

	private TableRenderer getTableRenderer(CyTable table) {
		if (nodeTableBrowser.getTableRenderer(table) != null)
			return nodeTableBrowser.getTableRenderer(table);
		if (edgeTableBrowser.getTableRenderer(table) != null)
			return edgeTableBrowser.getTableRenderer(table);
		if (networkTableBrowser.getTableRenderer(table) != null)
			return networkTableBrowser.getTableRenderer(table);
		if (globalTableBrowser.getTableRenderer(table) != null)
			return globalTableBrowser.getTableRenderer(table);
		
		return null;
	}
}
