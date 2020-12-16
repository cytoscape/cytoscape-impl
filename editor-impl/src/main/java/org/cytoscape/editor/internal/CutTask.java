package org.cytoscape.editor.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

public class CutTask extends AbstractTask {
	
	private final CyNetworkView netView;
	private final ClipboardManagerImpl clipMgr;
	private final Set<CyNode> selNodes;
	private final Set<CyEdge> selEdges;
	private final Map<CyEdge, Map<VisualProperty<?>, Object>/*bypass values*/> deletedEdges;
	private final CyServiceRegistrar serviceRegistrar;
	
	public CutTask(
			CyNetworkView netView,
			ClipboardManagerImpl clipMgr,
			CyServiceRegistrar serviceRegistrar
	) {
		this.netView = netView;
		this.clipMgr = clipMgr;
		this.serviceRegistrar = serviceRegistrar;
		
		// Get all of the selected nodes and edges
		selNodes = new HashSet<>(CyTableUtil.getNodesInState(netView.getModel(), CyNetwork.SELECTED, true));
		selEdges = new HashSet<>(CyTableUtil.getEdgesInState(netView.getModel(), CyNetwork.SELECTED, true));
		deletedEdges = new HashMap<>();
	}

	@SuppressWarnings("unchecked")
	public CutTask(
			CyNetworkView netView,
			View<? extends CyIdentifiable> objView,
			ClipboardManagerImpl clipMgr,
			CyServiceRegistrar serviceRegistrar
	) {
		// Get all of the selected nodes and edges first
		this(netView, clipMgr, serviceRegistrar);

		// Now, make sure we add our
		if (objView.getModel() instanceof CyNode)
			selNodes.add(((View<CyNode>) objView).getModel());
		else if (objView.getModel() instanceof CyEdge)
			selEdges.add(((View<CyEdge>) objView).getModel());
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		var lexicon = vmMgr.getAllVisualLexicon().iterator().next();
		var edgeProps = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		tm.setTitle("Cut Task");

		// Save edges that connect nodes that will be cut but are not selected to be cut themselves,
		// so they can be restored later on undo
		for (var node : selNodes) {
			var adjacentEdgeList = netView.getModel().getAdjacentEdgeList(node, CyEdge.Type.ANY);

			for (var edge : adjacentEdgeList) {
				if (!selEdges.contains(edge)) {
					deletedEdges.put(edge, new HashMap<VisualProperty<?>, Object>());

					// Save the bypass values for this edge
					var edgeView = netView.getEdgeView(edge);

					if (edgeView != null)
						ClipboardImpl.saveLockedValues(edgeView, edgeProps, deletedEdges);
				}
			}
		}
		
		var selAnnotations = serviceRegistrar.getService(AnnotationManager.class).getSelectedAnnotations(netView);
		
		clipMgr.cut(netView, selNodes, selEdges, selAnnotations);
		tm.setStatusMessage("Cut "+selNodes.size()+" nodes and "+selEdges.size()+" edges and copied them to the clipboard");
		
		var undoSupport = serviceRegistrar.getService(UndoSupport.class);
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
			clipMgr.cut(netView, clipboard.getNodes(), clipboard.getEdges(), clipboard.getAnnotations());
			netView.updateView();
		}

		@Override
		public void undo() {
			clipMgr.setCurrentClipboard(clipboard);
			var objects = new HashSet<>(clipMgr.paste(netView, clipboard.getCenterX(), clipboard.getCenterY()));
			
			// Restore edges that were not cut, but were deleted because their nodes were cut
			if (netView.getModel() instanceof CySubNetwork) {
				var net = (CySubNetwork) netView.getModel();
				
				for (var edge : deletedEdges.keySet()) {
					if (!net.containsEdge(edge)) {
						// Add edge back
						net.addEdge(edge);
						objects.add(edge);
					}
				}
			}
			
			var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
			eventHelper.flushPayloadEvents(); // Make sure views are created
			
			// Restore bypass values to deleted edges that were not cut
			for (var entry : deletedEdges.entrySet()) {
				var edgeView = netView.getEdgeView(entry.getKey());

				if (edgeView != null)
					ClipboardImpl.setLockedValues(edgeView, edgeView.getModel(), deletedEdges);
			}

			// Apply visual style to all restored nodes/edges
			var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
			var style = vmMgr.getVisualStyle(netView);

			for (var element : objects) {
				View<? extends CyIdentifiable> view = null;

				if (element instanceof CyNode)
					view = netView.getNodeView((CyNode) element);
				else if (element instanceof CyEdge)
					view = netView.getEdgeView((CyEdge) element);

				if (view != null)
					style.apply(netView.getModel().getRow(element), view);
			}

			netView.updateView();
		}
	}
}
