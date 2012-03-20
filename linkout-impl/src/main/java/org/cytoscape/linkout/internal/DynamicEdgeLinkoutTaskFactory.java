package org.cytoscape.linkout.internal;


import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;


public class DynamicEdgeLinkoutTaskFactory extends AbstractEdgeViewTaskFactory {

	private OpenBrowser browser;

	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView netView) {
		DynamicSupport support = new DynamicSupport(browser);
		support.setURLs(netView.getModel(),edgeView.getModel().getSource(), edgeView.getModel().getTarget());
		return support.createTaskIterator();
	}

	public DynamicEdgeLinkoutTaskFactory(OpenBrowser browser) {
		this.browser = browser;
	}
}
