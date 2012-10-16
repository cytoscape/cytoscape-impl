package org.cytoscape.editor.internal;


import java.util.ArrayList;
import java.util.List;

import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractNodeViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;


public class AddNestedNetworkTask extends AbstractNodeViewTask {

	private static final String HAS_NESTED_NETWORK_ATTRIBUTE = "has_nested_network";

	private final VisualMappingManager vmMgr;
	
	@Tunable(description="Network")
	public ListSingleSelection<CyNetwork> nestedNetwork;

	@ProvidesTitle
	public String getTitle() {
		return "Choose Network for Node";
	}
	
	public AddNestedNetworkTask(final View<CyNode> nv,
								final CyNetworkView view,
								final CyNetworkManager mgr,
								final VisualMappingManager vmMgr,
								final CyGroupManager grMgr) {
		super(nv,view);
		this.vmMgr = vmMgr;
		
		final List<CyNetwork> networks = new ArrayList<CyNetwork>(mgr.getNetworkSet());
		nestedNetwork = new ListSingleSelection<CyNetwork>(networks);
		final CyNetwork netPointer = nodeView.getModel().getNetworkPointer();
		
		if (netPointer != null && networks.contains(netPointer))
			nestedNetwork.setSelectedValue(netPointer);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNode node = nodeView.getModel();
		setNestedNetwork(node, nestedNetwork.getSelectedValue());
		
		final VisualStyle style = vmMgr.getVisualStyle(netView);
		style.apply(netView);
		netView.updateView();
	}
	
	private void setNestedNetwork(CyNode node, CyNetwork targetNetwork) {
		// TODO: We should consider exposing a nested network API so we don't
		// have to do this everywhere we establish this link.
		node.setNetworkPointer(targetNetwork);
		
		CyNetwork sourceNetwork = netView.getModel();
		CyTable nodeTable = sourceNetwork.getDefaultNodeTable();
		boolean attributeExists = nodeTable.getColumn(HAS_NESTED_NETWORK_ATTRIBUTE) != null;
		if (targetNetwork == null && attributeExists) {
			nodeTable.getRow(node.getSUID()).set(HAS_NESTED_NETWORK_ATTRIBUTE, false);
		} else if (targetNetwork != null) {
			if (!attributeExists) {
				nodeTable.createColumn(HAS_NESTED_NETWORK_ATTRIBUTE, Boolean.class, false);
			}
			CyRow row = nodeTable.getRow(node.getSUID());
			row.set(HAS_NESTED_NETWORK_ATTRIBUTE, true);
		}
	}

}
