package org.cytoscape.io.internal;

/*
 * #%L
 * Cytoscape GraphML Impl (graphml-impl)
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
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.internal.read.graphml.GraphMLFileFilter;
import org.cytoscape.io.internal.read.graphml.GraphMLReaderFactory;
import org.cytoscape.io.internal.write.graphml.GraphMLNetworkWriterFactory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.osgi.framework.BundleContext;

/**
 * Configurator/Activator of this bundle.
 */
public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) {
		// Import required Services
		StreamUtil streamUtilRef = getService(bc, StreamUtil.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc, CyLayoutAlgorithmManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyRootNetworkManager cyRootNetworkFactoryServiceRef = getService(bc, CyRootNetworkManager.class);
		CyNetworkManager cyNetworkManager = getService(bc, CyNetworkManager.class);
		
		GraphMLFileFilter graphMLFilter = new GraphMLFileFilter(new String[] { "graphml", "xml" }, new String[] {
				"text/graphml", "text/graphml+xml" }, "GraphML files", DataCategory.NETWORK, streamUtilRef);
		
		GraphMLReaderFactory graphMLReaderFactory = new GraphMLReaderFactory(graphMLFilter, cyLayoutsServiceRef,
				cyApplicationManagerServiceRef, cyNetworkFactoryServiceRef, cyNetworkManager,
				cyRootNetworkFactoryServiceRef);
		
		GraphMLNetworkWriterFactory graphMLNetworkWriterFactory = new GraphMLNetworkWriterFactory(graphMLFilter);

		registerService(bc, graphMLReaderFactory, InputStreamTaskFactory.class, new Properties());
		
		registerAllServices(bc, graphMLNetworkWriterFactory, new Properties());
	}
}
