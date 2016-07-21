package csapps.layout.algorithms.cose;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractPartitionLayoutTask;
import org.cytoscape.view.layout.LayoutEdge;
import org.cytoscape.view.layout.LayoutNode;
import org.cytoscape.view.layout.LayoutPartition;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.UndoSupport;
import org.ivis.layout.LEdge;
import org.ivis.layout.LGraph;
import org.ivis.layout.LGraphManager;
import org.ivis.layout.LGraphObject;
import org.ivis.layout.LNode;
import org.ivis.layout.LayoutOptionsPack;
import org.ivis.layout.ProgressListener;
import org.ivis.layout.Updatable;
import org.ivis.layout.cose.CoSELayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class CoSELayoutAlgorithmTask extends AbstractPartitionLayoutTask {

	private CoSELayout cose;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CoSELayoutAlgorithmTask.class);
	
	public CoSELayoutAlgorithmTask(
			final String displayName,
			final CyNetworkView networkView,
			final Set<View<CyNode>> nodesToLayOut,
			final CoSELayoutContext context,
			final UndoSupport undo,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(displayName, true, networkView, nodesToLayOut, "", undo);
		this.serviceRegistrar = serviceRegistrar;
		
		final LayoutOptionsPack.General generalOpt = LayoutOptionsPack.getInstance().getGeneral();
		generalOpt.layoutQuality = context.layoutQuality.getValue();
		generalOpt.incremental = context.incremental;
		
		final LayoutOptionsPack.CoSE coseOpt = LayoutOptionsPack.getInstance().getCoSE();
		coseOpt.idealEdgeLength = context.idealEdgeLength;
		coseOpt.springStrength = context.springStrength;
		coseOpt.repulsionStrength = context.repulsionStrength;
		coseOpt.gravityStrength = context.gravityStrength;
		coseOpt.compoundGravityStrength = context.compoundGravityStrength;
		coseOpt.gravityRange = context.gravityRange;
		coseOpt.compoundGravityRange = context.compoundGravityRange;
		coseOpt.smartEdgeLengthCalc = context.smartEdgeLengthCalc;
		coseOpt.smartRepulsionRangeCalc = context.smartRepulsionRangeCalc;
	}

	@Override
	public void layoutPartition(final LayoutPartition partition) {
		if (cancelled)
			return;
		
		final CyGroupManager groupManager = serviceRegistrar.getService(CyGroupManager.class);
		final CyNetwork network = networkView.getModel();
		
		// Create the CoSE model
		// (see http://www.cs.bilkent.edu.tr/~ivis/chilay/ChiLay-2.0-PG.pdf)
		cose = new CoSELayout();
		
		cose.addProgressListener(new ProgressListener() {
			@Override
			public void update(double value) {
				taskMonitor.setProgress(value);
			}
		});
		
		final LGraphManager gm = cose.getGraphManager();
		final LGraph root = gm.addRoot();
		
		// Index all LayoutNodes by CyNode for future reference
		final Map<CyNode, LayoutNode> layoutNodeMap = new HashMap<>();
		
		for (LayoutNode n : partition.getNodeList())
			layoutNodeMap.put(n.getNode(), n);
		
		// Create all CoSE nodes
		final Map<CyNode, LNode> lNodeMap = new HashMap<>();
		
		for (LayoutNode n : partition.getNodeList()) {
			// If this node does not belong to a CyGroup, let's traverse its potential compound node tree.
			if (groupManager.getGroupsForNode(n.getNode(), network).isEmpty())
				traverseLNodeTree(n, root, cose, lNodeMap, layoutNodeMap, groupManager);
		}
		
		if (cancelled)
			return;
		
		// Create all CoSE edges
		final Map<CyEdge, LEdge> lEdgeMap = new HashMap<>();
		final Iterator<LayoutEdge> edgeIter = partition.edgeIterator();

		while (edgeIter.hasNext() && !cancelled) {
			final LayoutEdge e = edgeIter.next();
			createLEdge(e, cose, lNodeMap, lEdgeMap);
		}
		
		if (cancelled)
			return;
		
		// Run the layout
		try {
			cose.runLayout();
		} catch (Exception e) {
			logger.error("Error running CoSE Layout", e);
			return;
		}
		
		if (cancelled)
			return;
		
		// Move all Node Views to the new positions
		for (LayoutNode n : partition.getNodeList())
			partition.moveNodeToLocation(n);
	}
	
	@Override
	public void cancel() {
		super.cancel();
		
		if (cose != null)
			cose.cancel();
	}

	private void traverseLNodeTree(
			final LayoutNode layoutNode,
			final LGraph graph,
			final CoSELayout cose,
			final Map<CyNode, LNode> lNodeMap,
			final Map<CyNode, LayoutNode> layoutNodeMap,
			final CyGroupManager groupManager
	) {
		if (lNodeMap.containsKey(layoutNode.getNode()))
			return; // This node has already been visited!
		
		final LNode ln = createLNode(layoutNode, graph, cose, lNodeMap);
		
		if (groupManager.isGroup(layoutNode.getNode(), networkView.getModel())) {
			final CyGroup group = groupManager.getGroup(layoutNode.getNode(), networkView.getModel());
			
			if (group != null) {
				final LGraphManager gm = cose.getGraphManager();
				final LGraph subGraph = gm.add(cose.newGraph("G" + group.getGroupNetwork().getSUID()), ln);
				
				for (CyNode childNode : group.getNodeList()) {
					final LayoutNode childLayoutNode = layoutNodeMap.get(childNode);
					
					if (childLayoutNode != null)
						traverseLNodeTree(childLayoutNode, subGraph, cose, lNodeMap, layoutNodeMap, groupManager);
				}
			}
		}
	}
	
	private LNode createLNode(
			final LayoutNode layoutNode,
			final LGraph graph,
			final CoSELayout cose,
			final Map<CyNode, LNode> lNodeMap
	) {
		final VNode vn = new VNode(layoutNode);
		final LNode ln = graph.add(cose.newNode(vn));
		ln.setCenter(layoutNode.getX(), layoutNode.getY());
		ln.setWidth(layoutNode.getWidth());
		ln.setHeight(layoutNode.getHeight());
		
		lNodeMap.put(layoutNode.getNode(), ln);
		
		return ln;
	}
	
	private LEdge createLEdge(
			final LayoutEdge layoutEdge,
			final CoSELayout cose,
			final Map<CyNode, LNode> lNodeMap,
			final Map<CyEdge, LEdge> lEdgeMap
	) {
		final LNode ln1 = lNodeMap.get(layoutEdge.getSource().getNode());
		final LNode ln2 = lNodeMap.get(layoutEdge.getTarget().getNode());
		
		if (ln1 != null && ln2 != null) {
			final VEdge ve = new VEdge(layoutEdge);
			final LEdge le = cose.getGraphManager().add(cose.newEdge(ve), ln1, ln2);
			
			lEdgeMap.put(layoutEdge.getEdge(), le);
			
			return le;
		}
		
		return null;
	}
	
	private class VNode implements Updatable {

		private final LayoutNode layoutNode;

		VNode(final LayoutNode layoutNode) {
			this.layoutNode = layoutNode;
		}
		
		@Override
		public void update(final LGraphObject go) {
			if (layoutNode != null) {
				// Update node's x and y coordinates
				final LNode ln = (LNode) go; 
				layoutNode.setX(ln.getCenterX());
				layoutNode.setY(ln.getCenterY());
			}
		}
		
		LayoutNode getLayoutNode() {
			return layoutNode;
		}
	}
	
	private class VEdge implements Updatable {

		private final LayoutEdge layoutEdge;

		VEdge(final LayoutEdge layoutEdge) {
			this.layoutEdge = layoutEdge;
		}
		
		@Override
		public void update(final LGraphObject go) {
			// TODO Update bend points
		}
		
		LayoutEdge getLayoutEdge() {
			return layoutEdge;
		}
	}
}
