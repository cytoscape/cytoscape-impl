package org.cytoscape.model.internal;

/*
 * #%L
 * Cytoscape Model Impl (model-impl)
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

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.equations.Interpreter;

import org.cytoscape.model.internal.CyTableFactoryImpl;
import org.cytoscape.model.internal.CyRootNetworkManagerImpl;
import org.cytoscape.model.internal.CyTableManagerImpl;
import org.cytoscape.model.internal.CyNetworkFactoryImpl;
import org.cytoscape.model.internal.CyNetworkManagerImpl;

import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNetworkFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) {
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		Interpreter InterpreterRef = getService(bc,Interpreter.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);

		TableEventHelperFacade tableEventHelper = new TableEventHelperFacade(cyEventHelperServiceRef);
		
		CyNetworkManagerImpl cyNetworkManager = new CyNetworkManagerImpl(cyServiceRegistrarServiceRef);
		CyNetworkTableManagerImpl cyNetworkTableManager = new CyNetworkTableManagerImpl();
		CyTableManagerImpl cyTableManager = new CyTableManagerImpl(cyEventHelperServiceRef,cyNetworkTableManager,cyNetworkManager);
		CyTableFactoryImpl cyTableFactory = new CyTableFactoryImpl(tableEventHelper,InterpreterRef,cyServiceRegistrarServiceRef);
		CyNetworkFactoryImpl cyNetworkFactory = new CyNetworkFactoryImpl(tableEventHelper,cyTableManager,cyNetworkTableManager,cyTableFactory,cyServiceRegistrarServiceRef);
		CyRootNetworkManagerImpl cyRootNetworkFactory = new CyRootNetworkManagerImpl();
		
		registerService(bc,cyNetworkFactory,CyNetworkFactory.class, new Properties());
		registerService(bc,cyTableFactory,CyTableFactory.class, new Properties());
		registerService(bc,cyRootNetworkFactory,CyRootNetworkManager.class, new Properties());
		registerService(bc,cyTableManager,CyTableManager.class, new Properties());
		registerAllServices(bc,cyNetworkTableManager, new Properties());
		registerService(bc,cyTableManager,NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc,cyNetworkManager,CyNetworkManager.class, new Properties());
	}
}
