



package org.cytoscape.equations.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;

import org.cytoscape.equations.internal.SUIDToEdgeMapper;
import org.cytoscape.equations.internal.SUIDToNodeMapper;
import org.cytoscape.equations.internal.FunctionRegistrar;



import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		EquationCompiler compilerServiceRef = getService(bc,EquationCompiler.class);
		CyApplicationManager applicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		
		SUIDToEdgeMapper suidToEdgeMapper = new SUIDToEdgeMapper();
		SUIDToNodeMapper suidToNodeMapper = new SUIDToNodeMapper();
		FunctionRegistrar functionRegistrar = new FunctionRegistrar(compilerServiceRef,applicationManagerServiceRef,suidToNodeMapper,suidToEdgeMapper);
		
		registerAllServices(bc,suidToEdgeMapper, new Properties());
		registerAllServices(bc,suidToNodeMapper, new Properties());

	}
}

