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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.view.legend.LegendDialog;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class CreateLegendTask extends AbstractTask {

	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;
	private final VisualMappingFunctionFactory continuousMappingFactory;

	public CreateLegendTask(final CyApplicationManager appManager, final VisualMappingManager vmm, VisualMappingFunctionFactory continuousMappingFactory) {
		this.appManager = appManager;
		this.vmm = vmm;
		this.continuousMappingFactory = continuousMappingFactory;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		// Should be executed in EDT!
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final VisualStyle selectedStyle = vmm.getCurrentVisualStyle();
				final LegendDialog ld = new LegendDialog(selectedStyle, appManager, vmm, continuousMappingFactory);
				ld.showDialog(null);
			}
		});
	}
}
