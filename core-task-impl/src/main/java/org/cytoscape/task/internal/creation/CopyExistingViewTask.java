package org.cytoscape.task.internal.creation;

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


import java.util.Collection;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * A utility task that copies the node positions and visual style to a new
 * network view from an existing network view.
 */
class CopyExistingViewTask extends AbstractTask {

	private final CyNetworkView newView;
	private final CyNetworkView sourceView;
	private final VisualMappingManager vmm;
	private RenderingEngineManager renderingEngineMgr;
	private final Map<CyNode,CyNode> new2sourceNodeMap;
	private final Map<CyEdge, CyEdge> new2sourceEdgeMap;
	private final boolean fitContent; 

	CopyExistingViewTask(final VisualMappingManager vmm,
						 final RenderingEngineManager renderingEngineMgr,
	                     final CyNetworkView newView, 
	                     final CyNetworkView sourceView, 
	                     final Map<CyNode, CyNode> new2sourceNodeMap /*may be null*/,
	                     final Map<CyEdge, CyEdge> new2sourceEdgeMap /*may be null*/,
	                     final boolean fitContent) {
		super();
		this.newView = newView;
		this.sourceView = sourceView;
		this.vmm = vmm;
		this.renderingEngineMgr = renderingEngineMgr;
		this.new2sourceNodeMap = new2sourceNodeMap;
		this.new2sourceEdgeMap = new2sourceEdgeMap;
		this.fitContent = fitContent;
	}

	@Override
	public void run(TaskMonitor tm) {
		if (sourceView == null)
			throw new NullPointerException("source network view is null.");
		if (newView == null)
			throw new NullPointerException("new network view is null.");

		tm.setProgress(0.0);
		
		final Collection<RenderingEngine<?>> engines = renderingEngineMgr.getAllRenderingEngines();
		Collection<VisualProperty<?>> nodeProps = null;
		Collection<VisualProperty<?>> edgeProps = null;
		
		if (!engines.isEmpty()) {
			final VisualLexicon lexicon = engines.iterator().next().getVisualLexicon();
			nodeProps = lexicon.getAllDescendants(BasicVisualLexicon.NODE);
			edgeProps = lexicon.getAllDescendants(BasicVisualLexicon.EDGE);
		}

		// Copy some network view properties
		if (!fitContent) {
			newView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION,
					sourceView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_X_LOCATION));
			newView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION,
					sourceView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION));
			newView.setVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION,
					sourceView.getVisualProperty(BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION));
			newView.setVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR,
					sourceView.getVisualProperty(BasicVisualLexicon.NETWORK_SCALE_FACTOR));
			newView.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH,
					sourceView.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH));
			newView.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT,
					sourceView.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT));
			newView.setVisualProperty(BasicVisualLexicon.NETWORK_DEPTH,
					sourceView.getVisualProperty(BasicVisualLexicon.NETWORK_DEPTH));
		}
		
		// Copy node locations and locked visual properties
		for (final View<CyNode> newNodeView : newView.getNodeViews()) {
			final View<CyNode> origNodeView = getOriginalNodeView(newNodeView); 
			
			if (origNodeView == null)
				continue;

			newNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
					origNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
			newNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
					origNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));

			if (nodeProps != null) {
				// Set lock (if necessary)
				for (final VisualProperty vp : nodeProps) {
					if (origNodeView.isValueLocked(vp))
						newNodeView.setLockedValue(vp, origNodeView.getVisualProperty(vp));
				}
			}
		}
		
		tm.setProgress(0.5);
		
		// Copy edge locked visual properties
		for (final View<CyEdge> newEdgeView : newView.getEdgeViews()) {
			final View<CyEdge> origEdgeView = getOriginalEdgeView(newEdgeView); 
			
			if (origEdgeView != null && edgeProps != null) {
				// Set lock (if necessary)
				for (final VisualProperty vp : edgeProps) {
					if (origEdgeView.isValueLocked(vp))
						newEdgeView.setLockedValue(vp, origEdgeView.getVisualProperty(vp));
				}
			}
		}
		
		tm.setProgress(0.9);

		final VisualStyle style = vmm.getVisualStyle(sourceView);
		vmm.setVisualStyle(style, newView);
		style.apply(newView);
		newView.updateView();
		
		if (fitContent)
			newView.fitContent();
		
		tm.setProgress(1.0);
	}

	// may return null if nodes don't somehow line up!
	private View<CyNode> getOriginalNodeView(View<CyNode> newNodeView) {
		if (new2sourceNodeMap != null) {
			CyNode origNode = new2sourceNodeMap.get(newNodeView.getModel());
			return origNode == null ? null : sourceView.getNodeView(origNode);
		} else {
			return sourceView.getNodeView(newNodeView.getModel());
		}
	}
	
	private View<CyEdge> getOriginalEdgeView(View<CyEdge> newEdgeView) {
		if (new2sourceEdgeMap != null) {
			CyEdge origEdge = new2sourceEdgeMap.get(newEdgeView.getModel());
			return origEdge == null ? null : sourceView.getEdgeView(origEdge);
		} else {
			return sourceView.getEdgeView(newEdgeView.getModel());
		}
	}
}
