package org.cytoscape.editor.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class GoToNestedNetworkTask extends AbstractNodeViewTask {

	private final CyNetworkManager netMgr;
	private final CyNetworkViewManager netViewMgr;
	private final CyApplicationManager appMgr;
	private final CreateNetworkViewTaskFactory createViewFactory;
	
	public GoToNestedNetworkTask(final View<CyNode> nv,
								 final CyNetworkView view,
								 final CyNetworkManager netMgr,
								 final CyNetworkViewManager netViewMgr,
								 final CyApplicationManager appMgr,
								 final CreateNetworkViewTaskFactory createViewFactory) {
		super(nv, view);
		this.netMgr = netMgr;
		this.netViewMgr = netViewMgr;
		this.appMgr = appMgr;
		this.createViewFactory = createViewFactory;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNode node = nodeView.getModel();
		final CyNetwork netPointer = node.getNetworkPointer();
		
		if (netPointer != null && netMgr.networkExists(netPointer.getSUID())) {
			CyNetworkView pointerView = null;
			Collection<CyNetworkView> views = netViewMgr.getNetworkViews(netPointer);
			
			for (CyNetworkView nv : views) {
				// Get the first view
				pointerView = nv;
				break;
			}
			
			if (pointerView == null) {
				// Create a network view
				final Set<CyNetwork> networks = new HashSet<CyNetwork>();
				networks.add(netPointer);
				TaskIterator iter = createViewFactory.createTaskIterator(networks);
				this.insertTasksAfterCurrentTask(iter);
			} else {
				// Just set the existing one as current
				appMgr.setCurrentNetworkView(pointerView);
			}
		}
	}
}
