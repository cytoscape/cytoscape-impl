package org.cytoscape.view.vizmap.gui.internal.util;

import java.util.Properties;

import org.cytoscape.service.util.CyServiceRegistrar;

// TODO: DELETE this class and use CyServiceRegistrar directly?

/**
 * Provides the required Cytoscape services and Tasks.
 */
public class ServicesUtil {

	private final CyServiceRegistrar cyServiceRegistrar;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public ServicesUtil(final CyServiceRegistrar cyServiceRegistrar) {
		this.cyServiceRegistrar = cyServiceRegistrar;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public CyServiceRegistrar getCyServiceRegistrar() {
		return cyServiceRegistrar;
	}

	public <T> T get(final Class<T> serviceClass) {
		return cyServiceRegistrar.getService(serviceClass);
	}
	
	public void registerAllServices(final Object service) {
		cyServiceRegistrar.registerAllServices(service, new Properties());
	}
	
	public void registerAllServices(final Object service, final Properties props) {
		cyServiceRegistrar.registerAllServices(service, props);
	}
	
	public void registerServiceListener(final Object listener, final String registerMethodName,
			final String unregisterMethodName, final Class<?> serviceClass) {
		cyServiceRegistrar.registerServiceListener(listener, registerMethodName, unregisterMethodName, serviceClass);
	}
}
