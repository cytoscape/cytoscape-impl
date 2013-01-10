package org.cytoscape.equations.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		EquationCompiler compilerServiceRef = getService(bc,EquationCompiler.class);
		CyApplicationManager applicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		
		FunctionRegistrar functionRegistrar = new FunctionRegistrar(compilerServiceRef,applicationManagerServiceRef);
		functionRegistrar.registerAllFunctions();
	}
}

