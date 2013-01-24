package org.cytoscape.view.vizmap.gui.internal.task;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ClearBendTask extends AbstractTask {

	private final View<CyEdge> edgeView;
	private final CyNetworkView netView;
	private final BendFactory bendFactory;

	private final VisualMappingManager vmm;

	ClearBendTask(View<CyEdge> edgeView, CyNetworkView netView, final VisualMappingManager vmm,
			final BendFactory bendFactory) {
		this.edgeView = edgeView;
		this.netView = netView;
		this.vmm = vmm;
		this.bendFactory = bendFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final VisualStyle style = vmm.getCurrentVisualStyle();
				final VisualMappingFunction<?, Bend> mapping = style
						.getVisualMappingFunction(BasicVisualLexicon.EDGE_BEND);
				if (mapping != null) {
					edgeView.setVisualProperty(BasicVisualLexicon.EDGE_BEND, bendFactory.createBend());
				} else {
					style.setDefaultValue(BasicVisualLexicon.EDGE_BEND, bendFactory.createBend());
					vmm.getCurrentVisualStyle().apply(netView);
				}
				netView.updateView();
			}
		});
	}
}
