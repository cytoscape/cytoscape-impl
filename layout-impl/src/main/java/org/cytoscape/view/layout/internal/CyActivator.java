

package org.cytoscape.view.layout.internal;

import org.cytoscape.property.CyProperty;
import org.cytoscape.work.undo.UndoSupport;

import org.cytoscape.view.layout.internal.CyLayoutsImpl;
import org.cytoscape.view.layout.internal.algorithms.GridNodeLayout;

import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;

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
		CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		
		CyLayoutsImpl cyLayouts = new CyLayoutsImpl(cyPropertyServiceRef);
		GridNodeLayout gridNodeLayout = new GridNodeLayout(undoSupportServiceRef);
		
		registerService(bc,cyLayouts,CyLayoutAlgorithmManager.class, new Properties());

		Properties gridNodeLayoutProps = new Properties();
		gridNodeLayoutProps.setProperty("preferredMenu","Layout.Cytoscape Layouts");
		gridNodeLayoutProps.setProperty("preferredTaskManager","menu");
		gridNodeLayoutProps.setProperty("title",gridNodeLayout.toString());
		registerService(bc,gridNodeLayout,CyLayoutAlgorithm.class, gridNodeLayoutProps);

		registerServiceListener(bc,cyLayouts,"addLayout","removeLayout",CyLayoutAlgorithm.class);
	}
}

