package org.cytoscape.sbml.internal;

/*
 * #%L
 * Cytoscape SBML Impl (sbml-impl)
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

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.view.model.CyNetworkViewFactory;

import org.cytoscape.sbml.internal.SBMLFileFilter;
import org.cytoscape.sbml.internal.SBMLNetworkViewTaskFactory;

import org.cytoscape.io.read.InputStreamTaskFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		StreamUtil streamUtilRef = getService(bc,StreamUtil.class);
		
		SBMLFileFilter sbmlFilter = new SBMLFileFilter("SBML files (*.xml)",streamUtilRef);
		SBMLNetworkViewTaskFactory sbmlNetworkViewTaskFactory = new SBMLNetworkViewTaskFactory(sbmlFilter,cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef);
		
		
		Properties sbmlNetworkViewTaskFactoryProps = new Properties();
		sbmlNetworkViewTaskFactoryProps.setProperty("readerDescription","SBML file reader");
		sbmlNetworkViewTaskFactoryProps.setProperty("readerId","sbmlNetworkViewReader");
		registerService(bc,sbmlNetworkViewTaskFactory,InputStreamTaskFactory.class, sbmlNetworkViewTaskFactoryProps);
	}
}

