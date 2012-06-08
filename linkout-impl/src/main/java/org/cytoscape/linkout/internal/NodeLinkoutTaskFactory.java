package org.cytoscape.linkout.internal;


import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;


public class NodeLinkoutTaskFactory extends AbstractNodeViewTaskFactory {

	private String link;
	private final OpenBrowser browser;

	public NodeLinkoutTaskFactory(OpenBrowser browser, String link) {
		super();
		this.link = link;
		this.browser = browser;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		return new TaskIterator(new LinkoutTask(link, browser, netView.getModel(), nodeView.getModel()));
	}
	
	public String getLink(){
		return link;
	}
	public void setLink (String link){
		this.link = link;
	}
}
