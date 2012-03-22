package org.cytoscape.editor.internal;

import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class AddNestedNetworkTaskFactory extends AbstractNodeViewTaskFactory {
	final CyNetworkManager netMgr;
	
	public AddNestedNetworkTaskFactory(CyNetworkManager netMgr) {
		this.netMgr = netMgr;
	}
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView,
			CyNetworkView networkView) {
		return new TaskIterator(new AddNestedNetworkTask(nodeView, networkView, netMgr));
		}

}
