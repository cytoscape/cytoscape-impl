/*
 File: NewNetworkSelectedNodesOnlyTask.java

 Copyright (c) 2006, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.task.internal.creation;


import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.undo.UndoSupport;


public class NewNetworkSelectedNodesOnlyTask extends AbstractNetworkFromSelectionTask {
	public NewNetworkSelectedNodesOnlyTask(final UndoSupport undoSupport, final CyNetwork net,
	                                       final CyRootNetworkManager cyroot,
	                                       final CyNetworkViewFactory cnvf,
	                                       final CyNetworkManager netmgr,
	                                       final CyNetworkViewManager networkViewManager,
	                                       final CyNetworkNaming cyNetworkNaming,
	                                       final VisualMappingManager vmm,
	                                       final CyApplicationManager appManager,
	                                       final CyEventHelper eventHelper,
	                                       final RenderingEngineManager renderingEngineMgr)
	{
		super(undoSupport, net, cyroot, cnvf, netmgr, networkViewManager, cyNetworkNaming,
		      vmm, appManager, eventHelper, renderingEngineMgr);
	}

	Collection<CyEdge> getEdges(CyNetwork netx, List<CyNode> nodes) {
		final Set<CyEdge> edges = new HashSet<CyEdge>();

		for (int i = 0; i < nodes.size(); i++) {
			CyNode n1 = nodes.get(i);
			for (int j = i ; j < nodes.size(); j++) {
				CyNode n2 = nodes.get(j);
				edges.addAll(netx.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY));
			}
		}
		return edges;
	}
}
