package org.cytoscape.search.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.search.internal.index.SearchManager;
import org.cytoscape.search.internal.ui.SearchBox;
import org.cytoscape.search.internal.ui.SearchBoxToolbarComponent;

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
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyActivator extends AbstractCyActivator {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	@Override
	public void start(BundleContext bc) {
		var registrar = getService(bc, CyServiceRegistrar.class);
		
		Path baseDir;
		try {
			baseDir = Files.createTempDirectory("search2_impl_");
			baseDir.toFile().deleteOnExit();
			System.out.println("Search index folder: " + baseDir);
		} catch (IOException e) {
			logger.error("Could not start search2-impl bundle. Cannot create temp folder for index files.", e);
			return;
		}
		
		var searchManager = new SearchManager(baseDir);
		var searchBox = new SearchBox(searchManager, registrar);
		var toolbarComponent = new SearchBoxToolbarComponent(searchBox);
		searchManager.setSearchBox(searchBox);
		
		registerAllServices(bc, searchManager);
		registerAllServices(bc, toolbarComponent);
	}
}
