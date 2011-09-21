



package org.cytoscape.linkout.internal;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.linkout.internal.LinkOut;
import org.cytoscape.property.BasicCyProperty;

import org.cytoscape.property.CyProperty;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CyActivator extends AbstractCyActivator {

	private static final Logger logger = LoggerFactory.getLogger(CyActivator.class);

	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		OpenBrowser openBrowserServiceRef = getService(bc,OpenBrowser.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
	
		Properties linkoutProperties = new Properties();
		try {
			linkoutProperties.load( getClass().getClassLoader().getResourceAsStream("linkout.props") );
		} catch (Exception e) {
			logger.warn("could not properly load linkout.props",e);
		}

		BasicCyProperty linkoutProps = new BasicCyProperty(linkoutProperties,CyProperty.SavePolicy.CONFIG_DIR);
		LinkOut linkout = new LinkOut(linkoutProps,cyServiceRegistrarServiceRef,openBrowserServiceRef,cyApplicationConfigurationServiceRef);
		
		Properties linkoutPropsProps = new Properties();
		linkoutPropsProps.setProperty("cyPropertyName","linkout");
		linkoutPropsProps.setProperty("serviceType","property");
		registerService(bc,linkoutProps,CyProperty.class, linkoutPropsProps);
	}
}

