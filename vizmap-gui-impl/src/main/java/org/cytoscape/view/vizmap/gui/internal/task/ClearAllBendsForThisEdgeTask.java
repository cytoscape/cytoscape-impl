package org.cytoscape.view.vizmap.gui.internal.task;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class ClearAllBendsForThisEdgeTask extends AbstractTask {

	private final View<CyEdge> edgeView;
	private final CyNetworkView netView;
	private final ServicesUtil servicesUtil;

	ClearAllBendsForThisEdgeTask(final View<CyEdge> edgeView, final CyNetworkView netView,
			final ServicesUtil servicesUtil) {
		this.edgeView = edgeView;
		this.netView = netView;
		this.servicesUtil = servicesUtil;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		SwingUtilities.invokeLater(() -> {
			final BendFactory bendFactory = servicesUtil.get(BendFactory.class);
			edgeView.setLockedValue(BasicVisualLexicon.EDGE_BEND, bendFactory.createBend());
			netView.updateView();
		});
	}
}
