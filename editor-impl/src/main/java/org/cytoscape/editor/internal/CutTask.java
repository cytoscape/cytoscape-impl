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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class CutTask extends AbstractTask {
	
	private final CyNetworkView netView;
	private final ClipboardManagerImpl clipMgr;
	private final VisualMappingManager vmMgr;
	private final UndoSupport undoSupport;
	private final CyEventHelper eventHelper;
	private final Set<CyNode> selNodes;
	private final Set<CyEdge> selEdges;
	private final Map<CyEdge, Map<VisualProperty<?>, Object>/*bypass values*/> deletedEdges;
	
	public CutTask(final CyNetworkView netView,
				   final ClipboardManagerImpl clipMgr,
				   final VisualMappingManager vmMgr,
	               final UndoSupport undoSupport,
	               final CyEventHelper eventHelper) {
		this.netView = netView;
		this.clipMgr = clipMgr;
		this.vmMgr = vmMgr;
		this.undoSupport = undoSupport;
		this.eventHelper = eventHelper;
		
		// Get all of the selected nodes and edges
		selNodes = new HashSet<CyNode>(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true));
		selEdges = new HashSet<CyEdge>(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true));
		deletedEdges = new HashMap<CyEdge, Map<VisualProperty<?>,Object>>();
	}

	@SuppressWarnings("unchecked")
	public CutTask(final CyNetworkView netView, final View<?extends CyIdentifiable> objView, 
	               final ClipboardManagerImpl clipMgr, final VisualMappingManager vmMgr,
	               final UndoSupport undoSupport, final CyEventHelper eventHelper) {
		// Get all of the selected nodes and edges first
		this(netView, clipMgr, vmMgr, undoSupport, eventHelper);

		// Now, make sure we add our
		if (objView.getModel() instanceof CyNode)
			selNodes.add(((View<CyNode>)objView).getModel());
		else if (objView.getModel() instanceof CyEdge)
			selEdges.add(((View<CyEdge>)objView).getModel());
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final VisualLexicon lexicon = vmMgr.getAllVisualLexicon().iterator().next();
		final Collection<VisualProperty<?>> edgeProps = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		tm.setTitle("Cut Task");
		
		// Save edges that connect nodes that will be cut but are not selected to be cut themselves,
		// so they can be restored later on undo
		for (CyNode node : selNodes) {
			List<CyEdge> adjacentEdgeList = netView.getModel().getAdjacentEdgeList(node, CyEdge.Type.ANY);
			
			for (CyEdge edge : adjacentEdgeList) {
				if (!selEdges.contains(edge)) {
					deletedEdges.put(edge, new HashMap<VisualProperty<?>, Object>());
					
					// Save the bypass values for this edge
					View<CyEdge> edgeView = netView.getEdgeView(edge);
					
					if (edgeView != null)
						ClipboardImpl.saveLockedValues(edgeView, edgeProps, deletedEdges);
				}
			}
		}
		
		clipMgr.cut(netView, selNodes, selEdges);
		tm.setStatusMessage("Cut "+selNodes.size()+" nodes and "+selEdges.size()+" edges and copied them to the clipboard");
		undoSupport.postEdit(new CutEdit());
	}
	
	private class CutEdit extends AbstractCyEdit {
		
		private final ClipboardImpl clipboard;

		public CutEdit() { 
			super("Cut");
			this.clipboard = clipMgr.getCurrentClipboard();
		}

		@Override
		public void redo() {
			clipMgr.cut(netView, clipboard.getNodes(), clipboard.getEdges());
			netView.updateView();
		}

		@Override
		public void undo() {
			clipMgr.setCurrentClipboard(clipboard);
			final HashSet<CyIdentifiable> objects = 
					new HashSet<CyIdentifiable>(clipMgr.paste(netView, clipboard.getCenterX(), clipboard.getCenterY()));
			
			// Restore edges that were not cut, but were deleted because their nodes were cut
			if (netView.getModel() instanceof CySubNetwork) {
				final CySubNetwork net = (CySubNetwork) netView.getModel();
				
				for (CyEdge edge : deletedEdges.keySet()) {
					if (!net.containsEdge(edge)) {
						// Add edge back
						net.addEdge(edge);
						objects.add(edge);
					}
				}
			}
			
			eventHelper.flushPayloadEvents(); // Make sure views are created
			
			// Restore bypass values to deleted edges that were not cut
			for (Entry<CyEdge, Map<VisualProperty<?>, Object>> entry : deletedEdges.entrySet()) {
				View<CyEdge> edgeView = netView.getEdgeView(entry.getKey());
				
				if (edgeView != null)
					ClipboardImpl.setLockedValues(edgeView, edgeView.getModel(), deletedEdges);
			}
			
			// Apply visual style to all restored nodes/edges
			final VisualStyle style = vmMgr.getVisualStyle(netView);
			
			for (CyIdentifiable element: objects) {
				View<? extends CyIdentifiable> view = null;
				
				if (element instanceof CyNode)
					view = netView.getNodeView((CyNode)element);
				else if (element instanceof CyEdge)
					view = netView.getEdgeView((CyEdge)element);

				if (view != null)
					style.apply(netView.getModel().getRow(element), view);
			}
			
			netView.updateView();
		}
	}
}
