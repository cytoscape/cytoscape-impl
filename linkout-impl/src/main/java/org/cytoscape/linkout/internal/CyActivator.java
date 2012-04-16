package org.cytoscape.linkout.internal;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.linkout.internal.LinkOut;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;

import org.osgi.framework.BundleContext;

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
		PropsReader linkoutProps = new PropsReader("linkout","linkout.props",CyProperty.SavePolicy.CONFIG_DIR);

		LinkOut linkout = new LinkOut(linkoutProps,cyServiceRegistrarServiceRef,openBrowserServiceRef,cyApplicationConfigurationServiceRef);
		
		Properties linkoutPropsProps = new Properties();
		linkoutPropsProps.setProperty("cyPropertyName","linkout");
		registerService(bc,linkoutProps,CyProperty.class, linkoutPropsProps);
	}
}

