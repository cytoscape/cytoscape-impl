package org.cytoscape.task.internal.vizmap;

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

import org.cytoscape.model.CyEdge;
import org.cytoscape.task.AbstractNetworkViewCollectionTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.EdgeBendVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskMonitor;

import java.util.Collection;

public class ClearAllEdgeBendsTask extends AbstractNetworkViewCollectionTask {

	private VisualMappingManager vmm;

	public ClearAllEdgeBendsTask(Collection<CyNetworkView> networkViews, VisualMappingManager vmm) {
		super(networkViews);
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		for (CyNetworkView networkView : networkViews) {
			networkView.setViewDefault(BasicVisualLexicon.EDGE_BEND, null);
			VisualStyle vs = vmm.getVisualStyle(networkView);
			vs.setDefaultValue(BasicVisualLexicon.EDGE_BEND, null);
			vs.removeVisualMappingFunction(BasicVisualLexicon.EDGE_BEND);
			final Collection<View<CyEdge>> edgeViews = networkView.getEdgeViews();
			for (final View<CyEdge> edgeView : edgeViews) {
				edgeView.setVisualProperty(BasicVisualLexicon.EDGE_BEND, null);
				edgeView.clearValueLock(BasicVisualLexicon.EDGE_BEND);
			}
			networkView.updateView();
		}
	}
}
