package org.cytoscape.filter.internal.filters.util;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskManager;

public class ServicesUtil {
	
	public static CySwingApplication cySwingApplicationServiceRef;
	
	public static CyApplicationManager cyApplicationManagerServiceRef;
	
	public static CyNetworkViewManager cyNetworkViewManagerServiceRef;
	public static CyNetworkManager cyNetworkManagerServiceRef;
	public static CyServiceRegistrar cyServiceRegistrarServiceRef;
	public static CyEventHelper cyEventHelperServiceRef;
	public static TaskManager taskManagerServiceRef;
	
	public static CyProperty<Properties> cytoscapePropertiesServiceRef;
	public static VisualMappingManager visualMappingManagerRef;
	public static CyNetworkFactory cyNetworkFactoryServiceRef;

	public static CyNetworkViewFactory cyNetworkViewFactoryServiceRef;
	public static CyLayoutAlgorithmManager cyLayoutsServiceRef;
	public static CyVersion cytoscapeVersionService;
	public static CyApplicationConfiguration cyApplicationConfigurationServiceRef;
}
