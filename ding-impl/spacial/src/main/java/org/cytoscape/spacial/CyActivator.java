



package org.cytoscape.spacial;


import org.cytoscape.spacial.internal.rtree.RTreeFactory;

import org.cytoscape.spacial.SpacialIndex2DFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {
		
		RTreeFactory rtreeFactory = new RTreeFactory();
		
		registerService(bc,rtreeFactory,SpacialIndex2DFactory.class, new Properties());
	}
}

