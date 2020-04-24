package org.cytoscape.browser.internal;

import static org.cytoscape.work.ServiceProperties.TITLE;

import java.awt.event.ActionListener;
import java.util.Properties;

import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.browser.internal.task.ClearAllErrorsTaskFactory;
import org.cytoscape.browser.internal.task.ColorColumnTestTaskFactory;
import org.cytoscape.browser.internal.task.SetColumnFormatTaskFactory;
import org.cytoscape.browser.internal.view.DefaultTableBrowser;
import org.cytoscape.browser.internal.view.GlobalTableBrowser;
import org.cytoscape.browser.internal.view.TableBrowserMediator;
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
import org.cytoscape.task.TableColumnTaskFactory;
import org.osgi.framework.BundleContext;

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

public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		var serviceRegistrar = getService(bc, CyServiceRegistrar.class);
//		var popupMenuHelper = new PopupMenuHelper(serviceRegistrar);
		
		var nodeTableBrowser = new DefaultTableBrowser("Node Table", CyNode.class, serviceRegistrar);
		var edgeTableBrowser = new DefaultTableBrowser("Edge Table", CyEdge.class, serviceRegistrar);
		var networkTableBrowser = new DefaultTableBrowser("Network Table", CyNetwork.class, serviceRegistrar);
		var globalTableBrowser = new GlobalTableBrowser("Unassigned Tables", serviceRegistrar);
		
		registerAllServices(bc, nodeTableBrowser);
		registerAllServices(bc, edgeTableBrowser);
		registerAllServices(bc, networkTableBrowser);

		registerService(bc, globalTableBrowser, ActionListener.class);
		registerService(bc, globalTableBrowser, SessionLoadedListener.class);
		registerService(bc, globalTableBrowser, SessionAboutToBeSavedListener.class);
		registerService(bc, globalTableBrowser, TableAboutToBeDeletedListener.class);
		registerService(bc, globalTableBrowser, TableAddedListener.class);
		registerService(bc, globalTableBrowser, TablePrivacyChangedListener.class);
		registerService(bc, globalTableBrowser, RowsSetListener.class);
		registerService(bc, globalTableBrowser, RowsDeletedListener.class);

//		registerServiceListener(bc, popupMenuHelper::addTableColumnTaskFactory, popupMenuHelper::removeTableColumnTaskFactory, TableColumnTaskFactory.class);
//		registerServiceListener(bc, popupMenuHelper::addTableCellTaskFactory, popupMenuHelper::removeTableCellTaskFactory, TableCellTaskFactory.class);
		
		{
			var factory = new ClearAllErrorsTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Clear All Errors");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
		}
		{
			var factory = new SetColumnFormatTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Format Column...");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
		}
		{
			var factory = new ColorColumnTestTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Test Cell Background Color...");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
		}
		
		var mediator = new TableBrowserMediator(nodeTableBrowser, edgeTableBrowser, networkTableBrowser,
				globalTableBrowser, serviceRegistrar);
		registerService(bc, mediator, SetCurrentNetworkListener.class);
		registerService(bc, mediator, CytoPanelComponentSelectedListener.class);
		
		
		// MKTODO
//		{
//			var factory = new HideColumnTaskFactory(mediator);
//			var props = new Properties();
//			props.setProperty(TITLE, "Hide Column");
//			// Do not register the factory as an OSGI service unless it's necessary.
//			// We just need to add it to the menu helper for now.
//			popupMenuHelper.addTableColumnTaskFactory(factory, props);
//		}
	}
}
