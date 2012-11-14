package org.cytoscape.ding;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SelectModeTask extends AbstractTask {

	private final CyApplicationManager applicationManagerServiceRef;
	private final String actionName;

	public SelectModeTask(final String actionName, final CyApplicationManager applicationManagerServiceRef){
		this.applicationManagerServiceRef = applicationManagerServiceRef;
		this.actionName = actionName;
	}
	
	@Override
	public void run(final TaskMonitor taskMonitor) {
		final CyNetworkView view = this.applicationManagerServiceRef.getCurrentNetworkView();

		if (view != null) {
			if (actionName.equalsIgnoreCase("Nodes only")) {
				view.clearValueLock(DVisualLexicon.NETWORK_NODE_SELECTION);
				view.setLockedValue(DVisualLexicon.NETWORK_EDGE_SELECTION, Boolean.FALSE);
			} else if (actionName.equalsIgnoreCase("Edges only")) {
				view.clearValueLock(DVisualLexicon.NETWORK_EDGE_SELECTION);
				view.setLockedValue(DVisualLexicon.NETWORK_NODE_SELECTION, Boolean.FALSE);
			} else if (actionName.equalsIgnoreCase("Nodes and Edges")) {
				view.clearValueLock(DVisualLexicon.NETWORK_NODE_SELECTION);
				view.clearValueLock(DVisualLexicon.NETWORK_EDGE_SELECTION);
			}
		}
	}
}
