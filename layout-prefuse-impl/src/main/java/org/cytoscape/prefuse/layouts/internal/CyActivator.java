
package org.cytoscape.prefuse.layouts.internal;

import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.prefuse.layouts.internal.ForceDirectedLayout;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import java.util.Properties;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		UndoSupport undoSupportServiceRef = getService(bc,UndoSupport.class);
		
		ForceDirectedLayout forceDirectedLayout = new ForceDirectedLayout(undoSupportServiceRef);
		
		Properties forceDirectedLayoutProps = new Properties();
		forceDirectedLayoutProps.setProperty("preferredMenu","Prefuse Layouts");
		registerService(bc,forceDirectedLayout,CyLayoutAlgorithm.class, forceDirectedLayoutProps);
	}
}

