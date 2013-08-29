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

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;


public class RemoveVisualStyleTask extends AbstractTask {

	private final VisualStyle style;
	private final ServicesUtil servicesUtil;
	private final Set<CyNetworkView> networkViews = new HashSet<CyNetworkView>();

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RemoveVisualStyleTask(final VisualStyle style, final ServicesUtil servicesUtil) {
		this.style = style;
		this.servicesUtil = servicesUtil;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public void run(final TaskMonitor taskMonitor) throws Exception {
		if (style != null) {
			removeVisualStyle();
			
			final UndoSupport undo = servicesUtil.get(UndoSupport.class);
			undo.postEdit(new RemoveVisualStyleEdit());
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void removeVisualStyle() {
		final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);

		if (vmMgr.getDefaultVisualStyle().equals(style))
			throw new IllegalArgumentException("You cannot delete the default style.");
		
		// First save the network views that have the style which is about to be deleted
		final CyNetworkViewManager netViewMgr = servicesUtil.get(CyNetworkViewManager.class);
		
		for (final CyNetworkView view : netViewMgr.getNetworkViewSet()) {
			if (style.equals(vmMgr.getVisualStyle(view)))
				networkViews.add(view);
		}
		
		// Now we can delete the visual style
		vmMgr.removeVisualStyle(style);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class RemoveVisualStyleEdit extends AbstractCyEdit {

		public RemoveVisualStyleEdit() {
			super("Remove Visual Style");
		}

		@Override
		public void undo() {
			// First register the visual style again
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
			
			if (style != null && !vmMgr.getAllVisualStyles().contains(style))
				vmMgr.addVisualStyle(style);
			
			// Now set the style to the saved views
			final CyNetworkViewManager netViewMgr = servicesUtil.get(CyNetworkViewManager.class);
			final Set<CyNetworkView> registeredViews = netViewMgr.getNetworkViewSet();
			
			for (final CyNetworkView view : networkViews) {
				if (registeredViews.contains(view)) // Check if this view is still registered
					vmMgr.setVisualStyle(style, view);
			}
		}

		@Override
		public void redo() {
			if (style != null)
				removeVisualStyle();
		}
	}
}
