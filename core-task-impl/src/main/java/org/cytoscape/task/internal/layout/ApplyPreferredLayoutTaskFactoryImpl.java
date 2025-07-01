package org.cytoscape.task.internal.layout;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.util.Collection;
import java.util.Collections;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

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

public class ApplyPreferredLayoutTaskFactoryImpl extends AbstractNetworkViewCollectionTaskFactory implements
		ApplyPreferredLayoutTaskFactory, TaskFactory {

	private final CyServiceRegistrar serviceRegistrar;
	private final boolean selectedOnly;

	public ApplyPreferredLayoutTaskFactoryImpl(CyServiceRegistrar serviceRegistrar, boolean selectedOnly) {
		this.serviceRegistrar = serviceRegistrar;
		this.selectedOnly = selectedOnly;
	}

	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetworkView> networkViews) {
		return new TaskIterator(2, new ApplyPreferredLayoutTask(serviceRegistrar, networkViews, selectedOnly));
	}

	@Override
	public TaskIterator createTaskIterator() {
		var networkViews = Collections.singletonList(serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView());
		return new TaskIterator(2, new ApplyPreferredLayoutTask(serviceRegistrar, networkViews, selectedOnly));
	}

	@Override
	public boolean isReady() {
		return true;
	}
	
	
	/**
	 * Returns true if the supplied collection is not null.
	 * @param networkViews The collection of network views.
	 * @return true if the supplied collection is not null.
	 */
	@Override
	public boolean isReady(Collection<CyNetworkView> networkViews) {
		if(networkViews == null || networkViews.isEmpty()) {
			return false;
		}
		if(!selectedOnly) {
			return true;
		}
		
		CyLayoutAlgorithmManager layoutMgr = serviceRegistrar.getService(CyLayoutAlgorithmManager.class);
		CyLayoutAlgorithm layout = layoutMgr.getDefaultLayout();
		if(!layout.getSupportsSelectedOnly()) {
			return false;
		}
		
		// All the network views need to have selected nodes
		for(var networkView : networkViews) {
			if(!hasSelectedNodes(networkView)) {
				return false;
			}
		}
		
		return true;
	}

	
	private static boolean hasSelectedNodes(CyNetworkView networkView) {
		CyNetwork network = networkView.getModel();
		CyTable nodeTable = network.getDefaultNodeTable();
		
		// fast way to check that there are no selected nodes
		if(nodeTable.countMatchingRows(CyNetwork.SELECTED, Boolean.TRUE) == 0) { 
			return false;
		}
		
		// There must be at least one selected node.
		// To be consistent with ApplyPreferredLayoutTask.getLayoutNodes() we also have to check for node visibility.
		for(View<CyNode> view : networkView.getNodeViews()) {
			var row = network.getRow(view.getModel());
			if(row.get(CyNetwork.SELECTED, Boolean.class) && view.getVisualProperty(NODE_VISIBLE)) {
				return true;
			}
		}
		
		return false;
	}
	
	
}
