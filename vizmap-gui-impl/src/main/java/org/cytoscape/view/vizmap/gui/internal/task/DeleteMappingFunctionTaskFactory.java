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

import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.vizmap.gui.VizMapGUI;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheet;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class DeleteMappingFunctionTaskFactory extends AbstractTaskFactory {

	private final ServicesUtil servicesUtil;

	public DeleteMappingFunctionTaskFactory(final ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	@Override
	public TaskIterator createTaskIterator() {
		Set<VisualPropertySheetItem<?>> selectedItems = null;
		final VizMapGUI gui = servicesUtil.get(VizMapGUI.class);
		
		if (gui instanceof VizMapperMainPanel) {
			final VisualPropertySheet vpSheet = ((VizMapperMainPanel)gui).getSelectedVisualPropertySheet();
			selectedItems = vpSheet.getSelectedItems();
		}
		
		return new TaskIterator(new DeleteMappingFunctionTask(selectedItems));
	}
	
	@Override
	public boolean isReady() {
		final VizMapGUI gui = servicesUtil.get(VizMapGUI.class);
		
		if (gui instanceof VizMapperMainPanel) {
			final VisualPropertySheet vpSheet = ((VizMapperMainPanel)gui).getSelectedVisualPropertySheet();
			
			if (vpSheet.getModel().getTargetDataType() != CyNetwork.class) {
				for (final VisualPropertySheetItem<?> item : vpSheet.getSelectedItems()) {
					if (item.getModel().getVisualMappingFunction() != null)
						return true;
				}
			}
		}
		
		return false;
	}
}