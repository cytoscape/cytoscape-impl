



package org.cytoscape.equations.internal;

/*
 * #%L
 * Cytoscape Equation Functions Impl (equations-functions-impl)
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

