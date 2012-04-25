package org.cytoscape.editor.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class GoToNestedNetworkTaskFactory extends AbstractNodeViewTaskFactory {
	
	private final CyNetworkManager netMgr;
	private final CyNetworkViewManager netViewMgr;
	private final CyApplicationManager appMgr;
	private final CreateNetworkViewTaskFactory createViewFactory;

	public GoToNestedNetworkTaskFactory(final CyNetworkManager netMgr,
										final CyNetworkViewManager netViewMgr,
										final CyApplicationManager appMgr,
										final CreateNetworkViewTaskFactory createViewFactory) {
		this.netMgr = netMgr;
		this.netViewMgr = netViewMgr;
		this.appMgr = appMgr;
		this.createViewFactory = createViewFactory;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(new GoToNestedNetworkTask(nodeView, networkView, netMgr, netViewMgr, appMgr, createViewFactory));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		if (!super.isReady(nodeView, networkView))
			return false;
		
		// Check if there is a network pointer and if it is registered.
		final CyNode node  = nodeView.getModel();
		final CyNetwork np = node.getNetworkPointer();
		
		return np != null && netMgr.networkExists(np.getSUID());
	}
}
