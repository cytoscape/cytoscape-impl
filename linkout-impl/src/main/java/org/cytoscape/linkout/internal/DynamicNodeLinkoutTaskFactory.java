package org.cytoscape.linkout.internal;


import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.model.CyNode;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;


public class DynamicNodeLinkoutTaskFactory extends DynamicSupport implements NodeViewTaskFactory {

	public void setNodeView(View<CyNode> nodeView, CyNetworkView netView) {
		setURLs(nodeView.getModel());
	}

	public DynamicNodeLinkoutTaskFactory(OpenBrowser browser) {
		super(browser);
	}
}
