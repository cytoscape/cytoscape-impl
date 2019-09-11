package org.cytoscape.browser.internal.view;

import java.awt.Component;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
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

public class TableBrowserMediator implements CytoPanelComponentSelectedListener {

	private final AbstractTableBrowser nodeTableBrowser;
	private final AbstractTableBrowser edgeTableBrowser;
	private final AbstractTableBrowser networkTableBrowser;
	private final AbstractTableBrowser globalTableBrowser;
	
	private final CyServiceRegistrar serviceRegistrar;

	public TableBrowserMediator(
			AbstractTableBrowser nodeTableBrowser,
			AbstractTableBrowser edgeTableBrowser,
			AbstractTableBrowser networkTableBrowser,
			AbstractTableBrowser globalTableBrowser,
			CyServiceRegistrar serviceRegistrar
	) {
		this.nodeTableBrowser = nodeTableBrowser;
		this.edgeTableBrowser = edgeTableBrowser;
		this.networkTableBrowser = networkTableBrowser;
		this.globalTableBrowser = globalTableBrowser;
		this.serviceRegistrar = serviceRegistrar;
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
}
