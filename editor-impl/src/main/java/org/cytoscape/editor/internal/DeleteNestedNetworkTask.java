package org.cytoscape.editor.internal;


import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeleteNestedNetworkTask extends AbstractNodeViewTask {

	private final SelectedVisualStyleManager svsmMgr;
	
	private static final Logger logger = LoggerFactory.getLogger(DeleteNestedNetworkTask.class);
	
	public DeleteNestedNetworkTask(final View<CyNode> nv,
								   final CyNetworkView view,
								   final CyNetworkManager mgr,
								   final SelectedVisualStyleManager svsmMgr) {
		super(nv,view);
		this.svsmMgr = svsmMgr;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNode n = nodeView.getModel();
		n.setNetworkPointer(null);
		
		nodeView.clearValueLock(BasicVisualLexicon.NODE_NESTED_NETWORK_IMAGE_VISIBLE);
		svsmMgr.getCurrentVisualStyle().apply(netView);
		netView.updateView();
	}
}
