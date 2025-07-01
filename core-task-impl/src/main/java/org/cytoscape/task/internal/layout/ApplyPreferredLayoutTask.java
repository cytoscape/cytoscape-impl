package org.cytoscape.task.internal.layout;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class ApplyPreferredLayoutTask extends AbstractNetworkViewCollectionTask {

	@Tunable(description = "Network view to apply the layout currently set as default to", 
			context = "nogui", 
			longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, 
			exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork networkSelected;
	
	@Tunable(description = "If true the layout will be applied only to selected nodes. "
			+ "Note, the layout must support this option, if not then the layout will be applied to all nodes.",
			context = "nogui")
	public boolean selectedOnly = false;
	

	private final CyServiceRegistrar serviceRegistrar;
	
	public ApplyPreferredLayoutTask(CyServiceRegistrar serviceRegistrar, Collection<CyNetworkView> networkViews, boolean selectedOnly) {
		super(networkViews);
		this.serviceRegistrar = serviceRegistrar;
		this.selectedOnly = selectedOnly;
	}


	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Apply Preferred Layout");
		tm.setProgress(0.0);

		final CyLayoutAlgorithmManager layoutMgr = serviceRegistrar.getService(CyLayoutAlgorithmManager.class);
		final CyLayoutAlgorithm layout = layoutMgr.getDefaultLayout();
		
		if (layout != null)
			tm.setStatusMessage("Applying " + layout.getName() + "...");
		else
			throw new IllegalArgumentException("Couldn't find default layout algorithm"); // Should not happen!
		
		Collection<CyNetworkView> views = networkViews;
		
		if (networkSelected != null)
			views = serviceRegistrar.getService(CyNetworkViewManager.class).getNetworkViews(networkSelected);
		
		tm.setProgress(0.1);
		
		int i = 0;
		int viewCount = views.size();
		
		for (CyNetworkView view : views) {
			if (cancelled)
				return;
			
			//clearEdgeBends(view);
			var nodes = getLayoutNodes(layout, view, selectedOnly);
			
			String layoutAttribute = layoutMgr.getLayoutAttribute(layout, view);
			TaskIterator tasks = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), nodes, layoutAttribute);
			
			if (tasks != null) // For unit tests...
				insertTasksAfterCurrentTask(tasks);

			i++;
			tm.setProgress((i / (double) viewCount));
		}

		tm.setProgress(1.0);
	}
	
	@SuppressWarnings({"rawtypes"})
	public Object getResults(Class type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> "{}";
			return res;
		}
		return null;
	}
	
	private static Set<View<CyNode>> getLayoutNodes(CyLayoutAlgorithm layout, CyNetworkView networkView, boolean selectedNodesOnly) {
		if (layout.getSupportsSelectedOnly() && selectedNodesOnly) {
			Set<View<CyNode>> nodeViews = new HashSet<>();
			CyNetwork network = networkView.getModel();
			for (View<CyNode> view : networkView.getNodeViews()) {
				if (network.getRow(view.getModel()).get(CyNetwork.SELECTED, Boolean.class) && view.getVisualProperty(NODE_VISIBLE)) {
					nodeViews.add(view);
				}
			}
			return nodeViews;
		}
		return CyLayoutAlgorithm.ALL_NODE_VIEWS;
	}

//	/**
//	 * Clears edge bend values ASSIGNED TO EACH EDGE. Default Edge Bend value
//	 * will not be cleared.
//	 * 
//	 * TODO: should we clear mapping, too?
//	 */
//	private final void clearEdgeBends(final CyNetworkView networkView) {
//		final Collection<View<CyEdge>> edgeViews = networkView.getEdgeViews();
//		if (edgeViews.isEmpty())
//			return;
//
//		final View<CyEdge> first = edgeViews.iterator().next();
//		if (first.isSet(BasicVisualLexicon.EDGE_BEND) == false)
//			return;
//
//		for (final View<CyEdge> edgeView : edgeViews) {
//			edgeView.setVisualProperty(BasicVisualLexicon.EDGE_BEND, null);
//			edgeView.clearValueLock(BasicVisualLexicon.EDGE_BEND);
//		}
//	}
}
