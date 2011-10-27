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
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;


public class NodeLinkoutTaskFactory extends AbstractNodeViewTaskFactory {

	private final String link;
	private final OpenBrowser browser;

	public NodeLinkoutTaskFactory(OpenBrowser browser, String link) {
		super();
		this.link = link;
		this.browser = browser;
	}

	public TaskIterator getTaskIterator() {
		return new TaskIterator(new LinkoutTask(link, browser, nodeView.getModel()));
	}
}
