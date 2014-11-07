package org.cytoscape.editor.internal;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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
import org.cytoscape.work.TaskMonitor;

public class DeleteNestedNetworkTask extends AbstractNodeViewTask {

	private static final String HAS_NESTED_NETWORK_ATTRIBUTE = "has_nested_network";
	
	private final VisualMappingManager vmMgr;
	
	public DeleteNestedNetworkTask(final View<CyNode> nv,
								   final CyNetworkView view,
								   final CyNetworkManager mgr,
								   final VisualMappingManager vmMgr) {
		super(nv,view);
		this.vmMgr = vmMgr;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final CyNode node = nodeView.getModel();
		setNestedNetwork(node, null);
		
		final VisualStyle style = vmMgr.getVisualStyle(netView);
		style.apply(netView.getModel().getRow(node), nodeView);
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
