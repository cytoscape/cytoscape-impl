package org.cytoscape.view.table.internal;

import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext bc) {
		System.out.println("table-presentation-impl start");
	}

}
