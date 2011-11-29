
package org.cytoscape.model.internal;

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
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNetworkFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		Interpreter InterpreterRef = getService(bc,Interpreter.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		
		CyNetworkManagerImpl cyNetworkManager = new CyNetworkManagerImpl(cyEventHelperServiceRef);
		CyNetworkTableManagerImpl cyNetworkTableManager = new CyNetworkTableManagerImpl();
		CyTableManagerImpl cyTableManager = new CyTableManagerImpl(cyEventHelperServiceRef,cyNetworkTableManager,cyNetworkManager);
		CyTableFactoryImpl cyTableFactory = new CyTableFactoryImpl(cyEventHelperServiceRef,InterpreterRef,cyServiceRegistrarServiceRef);
		CyNetworkFactoryImpl cyNetworkFactory = new CyNetworkFactoryImpl(cyEventHelperServiceRef,cyTableManager,cyNetworkTableManager,cyTableFactory,cyServiceRegistrarServiceRef);
		CyRootNetworkManagerImpl cyRootNetworkFactory = new CyRootNetworkManagerImpl();
		
		registerService(bc,cyNetworkFactory,CyNetworkFactory.class, new Properties());
		registerService(bc,cyTableFactory,CyTableFactory.class, new Properties());
		registerService(bc,cyRootNetworkFactory,CyRootNetworkManager.class, new Properties());
		registerService(bc,cyTableManager,CyTableManager.class, new Properties());
		registerService(bc,cyNetworkTableManager,CyNetworkTableManager.class, new Properties());
		registerService(bc,cyTableManager,NetworkAboutToBeDestroyedListener.class, new Properties());
		registerService(bc,cyNetworkManager,CyNetworkManager.class, new Properties());

	}
}

