
package org.cytoscape.prefuse.layouts.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		ForceDirectedLayout forceDirectedLayout = new ForceDirectedLayout();

        Properties forceDirectedLayoutProps = new Properties();
        forceDirectedLayoutProps.setProperty("preferredMenu","Layout.Prefuse Layouts");
        forceDirectedLayoutProps.setProperty("preferredTaskManager","menu");
        forceDirectedLayoutProps.setProperty("title",forceDirectedLayout.toString());
		registerService(bc,forceDirectedLayout,CyLayoutAlgorithm.class, forceDirectedLayoutProps);
	}
}

