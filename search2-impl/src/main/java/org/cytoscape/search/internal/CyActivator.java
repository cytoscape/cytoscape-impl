package org.cytoscape.search.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.search.internal.ui.NetworkSearchBox;
import org.cytoscape.search.internal.ui.NetworkSearchToolbarComponent;
import org.cytoscape.search.internal.ui.TableSearchAction;
import org.cytoscape.search.internal.ui.debug.DebugSearchProgressPanel;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyActivator extends AbstractCyActivator {

	public static final int ICON_WIDTH  = 24;
	public static final int ICON_HEIGHT = 24;
	public static final float ICON_FONT_SIZE = 18.0f;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	
	@Override
	public void start(BundleContext bc) {
		var registrar = getService(bc, CyServiceRegistrar.class);
		
		Path baseDir;
		try {
			// Karaf will create this folder under ~/CytoscapeConfiguration/3/karaf_data/tmp
			baseDir = Files.createTempDirectory("search2_impl_");
			System.out.println("Search index folder: " + baseDir);
		} catch (IOException e) {
			logger.error("Could not start search2-impl bundle. Cannot create temp folder for index files.", e);
			return;
		}
		
		var searchManager = new SearchManager(registrar, baseDir);
		registerAllServices(bc, searchManager);
		
		// Network search
		var searchBox = new NetworkSearchBox(registrar, searchManager);
		var toolbarComponent = new NetworkSearchToolbarComponent(searchBox);
		registerService(bc, toolbarComponent, ToolBarComponent.class);
		
		// This puts a search box directly in the toolbar of all the table browsers.
//		for(var type : List.of(CyNode.class, CyEdge.class, CyNetwork.class)) {  // add 'null' to list to support unassigned tables
//			var tableSearchBox = new TableSearchBox(registrar, searchManager);
//			var tableToolbarComponent = new TableSearchToolbarComponent(tableSearchBox, type);
// 			registerService(bc, tableToolbarComponent, TableToolBarComponent.class);
//		}
		
		// This puts a toolbar button in the table browser that shows a pop-up search box.
		var iconManager = getService(bc, IconManager.class);
		var iconFont = iconManager.getIconFont(ICON_FONT_SIZE);
		var icon = new TextIcon(IconManager.ICON_SEARCH, iconFont, ICON_WIDTH, ICON_HEIGHT);
		var tableSearchAction = new TableSearchAction(registrar, searchManager, icon, 0.0055f);
		registerService(bc, tableSearchAction, CyAction.class);
		
		// Debug panel
		if(DebugSearchProgressPanel.showDebugPanel(registrar)) {
			var debugPanel = new DebugSearchProgressPanel();
			searchManager.addProgressViewer(debugPanel);
			registerService(bc, debugPanel, CytoPanelComponent.class);
		}
	}
}
