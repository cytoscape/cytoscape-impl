package org.cytoscape.opencl.layout;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		UndoSupport undo = getService(bc,UndoSupport.class);

		CLLayout forceDirectedCLLayout = new CLLayout(undo);

        Properties forceDirectedCLLayoutProps = new Properties();
        forceDirectedCLLayoutProps.setProperty(PREFERRED_MENU, "Layout.Cytoscape Layouts");
        forceDirectedCLLayoutProps.setProperty("preferredTaskManager", "menu");
        forceDirectedCLLayoutProps.setProperty(TITLE, forceDirectedCLLayout.toString());
        forceDirectedCLLayoutProps.setProperty(MENU_GRAVITY, "10.5");
		registerService(bc, forceDirectedCLLayout, CyLayoutAlgorithm.class, forceDirectedCLLayoutProps);
	}
}

