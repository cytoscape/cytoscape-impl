package org.cytoscape.linkout.internal;


import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;


public class DynamicNodeLinkoutTaskFactory extends AbstractNodeViewTaskFactory {

	private OpenBrowser browser;

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		DynamicSupport support = new DynamicSupport(browser);
		support.setURLs(netView.getModel(),nodeView.getModel());
		return support.createTaskIterator();
	}

	public DynamicNodeLinkoutTaskFactory(OpenBrowser browser) {
		this.browser = browser;
	}
}
