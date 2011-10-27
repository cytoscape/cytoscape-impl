package org.cytoscape.linkout.internal;


import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.model.CyEdge;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;


public class DynamicEdgeLinkoutTaskFactory extends DynamicSupport implements EdgeViewTaskFactory {

	public void setEdgeView(View<CyEdge> edgeView, CyNetworkView netView) {
		setURLs(edgeView.getModel().getSource(), edgeView.getModel().getTarget());
	}

	public DynamicEdgeLinkoutTaskFactory(OpenBrowser browser) {
		super(browser);
	}
}
