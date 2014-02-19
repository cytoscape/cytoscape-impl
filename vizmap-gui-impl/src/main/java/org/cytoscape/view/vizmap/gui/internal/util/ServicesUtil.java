package org.cytoscape.view.vizmap.gui.internal.util;

import java.util.Properties;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.puremvc.java.multicore.patterns.facade.Facade;


/**
 * Provides the required Cytoscape services and Tasks.
 */
public class ServicesUtil {

	private final CyServiceRegistrar cyServiceRegistrar;
	private final String facadeName;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public ServicesUtil(final CyServiceRegistrar cyServiceRegistrar, final String facadeName) {
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.facadeName = facadeName;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	public CyServiceRegistrar getCyServiceRegistrar() {
		return cyServiceRegistrar;
	}

	public <T> T get(final Class<T> serviceClass) {
		return cyServiceRegistrar.getService(serviceClass);
	}
	
	public <T> T get(final Class<T> serviceClass, final String filter) {
		return cyServiceRegistrar.getService(serviceClass, filter);
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
	
	/**
	 * Create and send an <code>INotification</code>.
	 * @param notificationName the name of the notification to send
	 * @param body the body of the notification (optional)
	 * @param type the type of the notification (optional)
	 */ 
	public void sendNotification(final String notificationName, final Object body, final String type ) {
		Facade.getInstance(facadeName).sendNotification(notificationName, body, type);
	}
	
	/**
	 * Create and send an <code>INotification</code>.
	 * @param notificationName the name of the notification to send
	 * @param body the body of the notification (optional)
	 */ 
	public void sendNotification(final String notificationName, final Object body) {
		Facade.getInstance(facadeName).sendNotification(notificationName, body);
	}
	
	/**
	 * Create and send an <code>INotification</code>.
	 * @param notificationName the name of the notification to send
	 */ 
	public void sendNotification(final String notificationName) {
		Facade.getInstance(facadeName).sendNotification(notificationName);
	}
}
