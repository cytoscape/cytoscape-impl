package org.cytoscape.task.internal.hide;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo hiding of nodes and edges. */
final class HideEdit extends AbstractCyEdit {
	
	private final CyNetworkView netView;
	private final Collection<? extends CyIdentifiable> elements;
	private final boolean visible;
	private final Map<View<? extends CyIdentifiable>, Boolean> previousStates;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmMgr;

	HideEdit(final String description, final CyNetworkView netView,
	         final Collection<? extends CyIdentifiable> elements, final boolean visible,
	         final CyEventHelper eventHelper, final VisualMappingManager vmMgr) {
		super(description);

		this.netView = netView;
		this.elements = elements;
		this.visible = visible;
		this.eventHelper = eventHelper;
		this.vmMgr = vmMgr;
		
		previousStates = new HashMap<>();

		// Save current visible values
		for (final CyIdentifiable model : elements) {
			if (model instanceof CyNode) {
				final View<CyNode> view = netView.getNodeView((CyNode)model);
				
				if (view != null)
					previousStates.put(view, view.getVisualProperty(NODE_VISIBLE));
				
				if (!visible) {
					final CyNode n1 = (CyNode) model;
					final CyNetwork net = netView.getModel();
					
					for (final CyNode n2 : net.getNeighborList(n1, CyEdge.Type.ANY)) {
						for (final CyEdge e : net.getConnectingEdgeList(n1, n2, CyEdge.Type.ANY)) {
							final View<CyEdge> ev = netView.getEdgeView(e);
							
							if (ev != null)
								previousStates.put(ev, ev.getVisualProperty(EDGE_VISIBLE));
						}
					}
				}
			} else if (model instanceof CyEdge) {
				final View<CyEdge> view = netView.getEdgeView((CyEdge)model);
				
				if (view != null)
					previousStates.put(view, view.getVisualProperty(EDGE_VISIBLE));
			}
		}
	}

	@Override
	public void undo() {
		for (final View<? extends CyIdentifiable> view : previousStates.keySet()) {
			setVisible(view, previousStates.get(view));
		}
		
		updateNetworkView();
	}
	
	@Override
	public void redo() {
		for (final CyIdentifiable model : elements) {
			View<? extends CyIdentifiable> view = null;
			
			if (model instanceof CyNode) {
				view = netView.getNodeView((CyNode)model);
			} else if (model instanceof CyEdge) {
				view = netView.getEdgeView((CyEdge)model);
			}
			
			if (view != null)
				setVisible(view, visible);
		}
		
		updateNetworkView();
	}

	private void setVisible(final View<? extends CyIdentifiable> view, final boolean visible) {
		final VisualProperty<Boolean> vp = view.getModel() instanceof CyNode ? NODE_VISIBLE : EDGE_VISIBLE;

		if (visible)
			view.clearValueLock(vp);
		else
			view.setLockedValue(vp, false);
	}

	private void updateNetworkView() {
		eventHelper.flushPayloadEvents();
		
		vmMgr.getVisualStyle(netView).apply(netView);
		netView.updateView();
	}
}
