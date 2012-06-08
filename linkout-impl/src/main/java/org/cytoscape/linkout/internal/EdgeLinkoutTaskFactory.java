package org.cytoscape.linkout.internal;


import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;


public class EdgeLinkoutTaskFactory extends AbstractEdgeViewTaskFactory {

	private String link;
	private final OpenBrowser browser;

	public EdgeLinkoutTaskFactory(OpenBrowser browser, String link) {
		super();
		this.link = link;
		this.browser = browser;
	}

	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView netView) {
		return new TaskIterator(new LinkoutTask(link, browser, netView.getModel(), edgeView.getModel().getSource(), edgeView.getModel().getTarget(), edgeView.getModel()));
	}
	
	public String getLink(){
		return link;
	}
	public void setLink (String link){
		this.link = link;
	}
}
