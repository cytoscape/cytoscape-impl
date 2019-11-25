package org.cytoscape.task.internal.view;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Y_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_CENTER_Z_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_DEPTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.util.Collection;
import java.util.Map;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

/**
 * A utility task that copies the node positions and visual style to a new
 * network view from an existing network view.
 */
public class CopyExistingViewTask extends AbstractTask implements ObservableTask {

	private final CyNetworkView newView;
	private final CyNetworkView sourceView;
	private final VisualStyle style;
	private final Map<CyNode,CyNode> new2sourceNodeMap;
	private final Map<CyEdge, CyEdge> new2sourceEdgeMap;
	private final boolean fitContent; 
	
	private final CyServiceRegistrar serviceRegistrar;

	public CopyExistingViewTask(
			CyNetworkView newView,
			CyNetworkView sourceView,
			VisualStyle style,
			Map<CyNode, CyNode> new2sourceNodeMap /* may be null */,
			Map<CyEdge, CyEdge> new2sourceEdgeMap /* may be null */,
			boolean fitContent,
			CyServiceRegistrar serviceRegistrar
	) {
		this.newView = newView;
		this.sourceView = sourceView;
		this.style = style;
		this.new2sourceNodeMap = new2sourceNodeMap;
		this.new2sourceEdgeMap = new2sourceEdgeMap;
		this.fitContent = fitContent;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Copy Existing Network View");
		tm.setProgress(0.0);
		
		if (sourceView == null)
			throw new NullPointerException("source network view is null.");
		if (newView == null)
			throw new NullPointerException("new network view is null.");

		var renderingEngineMgr = serviceRegistrar.getService(RenderingEngineManager.class);
		var engines = renderingEngineMgr.getAllRenderingEngines();
		Collection<VisualProperty<?>> nodeProps = null;
		Collection<VisualProperty<?>> edgeProps = null;
		
		if (!engines.isEmpty()) {
			final VisualLexicon lexicon = engines.iterator().next().getVisualLexicon();
			nodeProps = lexicon.getAllDescendants(NODE);
			edgeProps = lexicon.getAllDescendants(EDGE);
		}

		// Copy some network view properties
		if (!fitContent) {
			newView.setVisualProperty(NETWORK_CENTER_X_LOCATION, sourceView.getVisualProperty(NETWORK_CENTER_X_LOCATION));
			newView.setVisualProperty(NETWORK_CENTER_Y_LOCATION, sourceView.getVisualProperty(NETWORK_CENTER_Y_LOCATION));
			newView.setVisualProperty(NETWORK_CENTER_Z_LOCATION, sourceView.getVisualProperty(NETWORK_CENTER_Z_LOCATION));
			newView.setVisualProperty(NETWORK_SCALE_FACTOR, sourceView.getVisualProperty(NETWORK_SCALE_FACTOR));
			newView.setVisualProperty(NETWORK_WIDTH, sourceView.getVisualProperty(NETWORK_WIDTH));
			newView.setVisualProperty(NETWORK_HEIGHT, sourceView.getVisualProperty(NETWORK_HEIGHT));
			newView.setVisualProperty(NETWORK_DEPTH, sourceView.getVisualProperty(NETWORK_DEPTH));
		}
		
		// Copy node locations and locked visual properties
		tm.setStatusMessage("Copying node views...");
		tm.setProgress(0.1);
		
		for (var newNodeView : newView.getNodeViews()) {
			if (cancelled)
				return;
			
			var origNodeView = getOriginalNodeView(newNodeView); 
			
			if (origNodeView == null)
				continue;

			newNodeView.setVisualProperty(NODE_X_LOCATION, origNodeView.getVisualProperty(NODE_X_LOCATION));
			newNodeView.setVisualProperty(NODE_Y_LOCATION, origNodeView.getVisualProperty(NODE_Y_LOCATION));

			if (nodeProps != null) {
				// Set lock (if necessary)
				for (VisualProperty<?> vp : nodeProps) {
					if (origNodeView.isValueLocked(vp))
						newNodeView.setLockedValue(vp, origNodeView.getVisualProperty(vp));
				}
			}
		}
		
		// Copy edge locked visual properties
		tm.setStatusMessage("Copying edge views...");
		tm.setProgress(0.5);
		
		for (var newEdgeView : newView.getEdgeViews()) {
			if (cancelled)
				return;
			
			var origEdgeView = getOriginalEdgeView(newEdgeView); 
			
			if (origEdgeView != null && edgeProps != null) {
				// Set lock (if necessary)
				for (VisualProperty<?> vp : edgeProps) {
					if (origEdgeView.isValueLocked(vp))
						newEdgeView.setLockedValue(vp, origEdgeView.getVisualProperty(vp));
				}
			}
		}
		
		tm.setStatusMessage("Applying style...");
		tm.setProgress(0.9);
		
		if (cancelled)
			return;
		
		if (style != null) {
			style.apply(newView);
			newView.updateView();
		}
		
		if (cancelled)
			return;
		
		if (fitContent)
			newView.fitContent();

		tm.setProgress(1.0);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getResults(Class type) {
		return String.class.equals(type) ? newView.toString() : newView;
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
