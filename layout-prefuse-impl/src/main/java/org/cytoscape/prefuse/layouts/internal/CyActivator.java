
package org.cytoscape.prefuse.layouts.internal;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		UndoSupport undo = getService(bc,UndoSupport.class);

		ForceDirectedLayout forceDirectedLayout = new ForceDirectedLayout(undo);

        Properties forceDirectedLayoutProps = new Properties();
        forceDirectedLayoutProps.setProperty(PREFERRED_MENU,"Layout.Cytoscape Layouts");
        forceDirectedLayoutProps.setProperty("preferredTaskManager","menu");
        forceDirectedLayoutProps.setProperty(TITLE,forceDirectedLayout.toString());
        forceDirectedLayoutProps.setProperty(MENU_GRAVITY,"10.5");
		registerService(bc,forceDirectedLayout,CyLayoutAlgorithm.class, forceDirectedLayoutProps);
	}
}

