package org.cytoscape.task.internal.vizmap;

import java.util.Collection;

import org.cytoscape.model.CyEdge;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.task.internal.utils.DataUtils;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;

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

public class ClearAllEdgeBendsTask extends AbstractNetworkViewCollectionTask {

	private final CyServiceRegistrar serviceRegistrar;

	public ClearAllEdgeBendsTask(Collection<CyNetworkView> networkViews, CyServiceRegistrar serviceRegistrar) {
		super(networkViews);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Clear All Edge Bends");
		tm.setProgress(0.0);
		
		final VisualMappingManager vmManager = serviceRegistrar.getService(VisualMappingManager.class);
		int count = 1;
		int total = networkViews.size();
		
		for (CyNetworkView nv : networkViews) {
			tm.setStatusMessage(count + " of " + total + ": " + DataUtils.getViewTitle(nv) + "...");
			tm.setProgress((float)count / total);
			
			nv.setViewDefault(BasicVisualLexicon.EDGE_BEND, null);
			
			VisualStyle vs = vmManager.getVisualStyle(nv);
			vs.setDefaultValue(BasicVisualLexicon.EDGE_BEND, null);
			vs.removeVisualMappingFunction(BasicVisualLexicon.EDGE_BEND);
			
			final Collection<View<CyEdge>> edgeViews = nv.getEdgeViews();
			
			for (final View<CyEdge> ev : edgeViews) {
				ev.setVisualProperty(BasicVisualLexicon.EDGE_BEND, null);
				ev.clearValueLock(BasicVisualLexicon.EDGE_BEND);
			}
			
			nv.updateView();
			count++;
		}
		
		tm.setProgress(1.0);
	}
}
