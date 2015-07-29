package org.cytoscape.browser.internal;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

import java.awt.event.ActionListener;
import java.util.Properties;

import org.cytoscape.browser.internal.view.AbstractTableBrowser;
import org.cytoscape.browser.internal.view.DefaultTableBrowser;
import org.cytoscape.browser.internal.view.GlobalTableBrowser;
import org.cytoscape.browser.internal.view.PopupMenuHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.RowsDeletedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);

		PopupMenuHelper popupMenuHelper = new PopupMenuHelper(cyServiceRegistrarServiceRef);
		
		AbstractTableBrowser nodeTableBrowser = new DefaultTableBrowser("Node Table", CyNode.class, cyServiceRegistrarServiceRef, popupMenuHelper);
		AbstractTableBrowser edgeTableBrowser = new DefaultTableBrowser("Edge Table", CyEdge.class, cyServiceRegistrarServiceRef, popupMenuHelper);
		AbstractTableBrowser networkTableBrowser = new DefaultTableBrowser("Network Table", CyNetwork.class, cyServiceRegistrarServiceRef, popupMenuHelper);
		AbstractTableBrowser globalTableBrowser = new GlobalTableBrowser("Unassigned Tables", cyServiceRegistrarServiceRef, popupMenuHelper);
		
		registerAllServices(bc, nodeTableBrowser, new Properties());
		registerAllServices(bc, edgeTableBrowser, new Properties());
		registerAllServices(bc, networkTableBrowser, new Properties());

		final Properties globalTableProp = new Properties();
		registerService(bc, globalTableBrowser, ActionListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, SessionLoadedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, SessionAboutToBeSavedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, TableAboutToBeDeletedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, TableAddedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, TablePrivacyChangedListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, RowsSetListener.class, globalTableProp);
		registerService(bc, globalTableBrowser, RowsDeletedListener.class, globalTableProp);

		registerServiceListener(bc, popupMenuHelper, "addTableColumnTaskFactory", "removeTableColumnTaskFactory", TableColumnTaskFactory.class);
		registerServiceListener(bc, popupMenuHelper, "addTableCellTaskFactory", "removeTableCellTaskFactory", TableCellTaskFactory.class);
	}
}
