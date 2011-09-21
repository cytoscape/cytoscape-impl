
package org.cytoscape.sbml.internal;

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
		sbmlNetworkViewTaskFactoryProps.setProperty("serviceType","sbmlNetworkViewTaskFactory");
		sbmlNetworkViewTaskFactoryProps.setProperty("readerDescription","SBML file reader");
		sbmlNetworkViewTaskFactoryProps.setProperty("readerId","sbmlNetworkViewReader");
		registerService(bc,sbmlNetworkViewTaskFactory,InputStreamTaskFactory.class, sbmlNetworkViewTaskFactoryProps);
	}
}

