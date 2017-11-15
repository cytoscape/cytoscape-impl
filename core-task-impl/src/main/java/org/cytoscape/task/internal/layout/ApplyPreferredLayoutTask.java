package org.cytoscape.task.internal.layout;

import java.util.Collection;
import java.util.Collections;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
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
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

	@Tunable(description = "Network view to apply the layout currently set as default to", context = "nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork networkSelected;

	private final CyServiceRegistrar serviceRegistrar;
	
	public ApplyPreferredLayoutTask(Collection<CyNetworkView> networkViews, CyServiceRegistrar serviceRegistrar) {
		super(networkViews);
		this.serviceRegistrar = serviceRegistrar;
	}

	public ApplyPreferredLayoutTask(CyServiceRegistrar serviceRegistrar) {
		this(Collections.singletonList(serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView()),
				serviceRegistrar);
	}

	@Override
	public void run(TaskMonitor tm) {
		tm.setProgress(0.0d);
		tm.setStatusMessage("Applying Default Layout...");

		final CyNetworkViewManager viewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		Collection<CyNetworkView> views = networkViews;
		
		if (networkSelected != null)
			views = viewMgr.getNetworkViews(networkSelected);
		
		final CyLayoutAlgorithmManager layoutMgr = serviceRegistrar.getService(CyLayoutAlgorithmManager.class);
		final CyLayoutAlgorithm layout = layoutMgr.getDefaultLayout();
		tm.setProgress(0.2);
		
		int i = 0;
		int viewCount = views.size();
		
		for (final CyNetworkView view : views) {
			if (layout != null) {
				//clearEdgeBends(view);
				final TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
						CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
				
				if (itr != null) // For unit tests...
					insertTasksAfterCurrentTask(itr);
			} else {
				throw new IllegalArgumentException("Couldn't find default layout algorithm");
			}

			i++;
			tm.setProgress((i / (double) viewCount));
		}

		tm.setProgress(1.0);
	}
	
	@SuppressWarnings({"rawtypes"})
	public Object getResults(Class type) {
		if (type.equals(JSONResult.class)) {
			JSONResult res = () -> { return "{}"; };
			return res;
		}
		return null;
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
