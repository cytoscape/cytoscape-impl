package org.cytoscape.view.vizmap.gui.internal.task;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

public class RemoveVisualStylesTask extends AbstractTask {

	public static final String TITLE = "Remove Style";
	
	private final Set<VisualStyle> styles = new HashSet<>();
	private final ServicesUtil servicesUtil;
	private final Set<CyNetworkView> networkViews = new HashSet<>();

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public RemoveVisualStylesTask(VisualStyle style, ServicesUtil servicesUtil) {
		this(Collections.singleton(style), servicesUtil);
	}
	
	public RemoveVisualStylesTask(Set<VisualStyle> styles, ServicesUtil servicesUtil) {
		if (styles != null && !styles.isEmpty())
			this.styles.addAll(styles);
		
		this.servicesUtil = servicesUtil;
	}
	
	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@ProvidesTitle
	public String getTitle() {
		return TITLE + (styles.size() > 1 ? "s" : "");
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (!styles.isEmpty()) {
			removeVisualStyles();
			
			var undo = servicesUtil.get(UndoSupport.class);
			undo.postEdit(new RemoveVisualStylesEdit());
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void removeVisualStyles() {
		var vmMgr = servicesUtil.get(VisualMappingManager.class);
		var defStyle = vmMgr.getDefaultVisualStyle();
		
		var netViewMgr = servicesUtil.get(CyNetworkViewManager.class);
		var viewSet = netViewMgr.getNetworkViewSet();

		for (var vs : styles) {
			if (defStyle.equals(vs)) {
				if (styles.size() == 1)
					throw new IllegalArgumentException("You cannot delete the default style.");
				else
					continue; // Simply ignore the default style when deleting multiple styles at once
			}
			
			// First save the network views that have the style which is about to be deleted
			for (var view : viewSet) {
				if (vs.equals(vmMgr.getVisualStyle(view)))
					networkViews.add(view);
			}
			
			// Now we can delete the visual style
			vmMgr.removeVisualStyle(vs);
		}
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class RemoveVisualStylesEdit extends AbstractCyEdit {

		public RemoveVisualStylesEdit() {
			super(getTitle());
		}

		@Override
		public void undo() {
			var vmMgr = servicesUtil.get(VisualMappingManager.class);
			var netViewMgr = servicesUtil.get(CyNetworkViewManager.class);
			var viewSet = netViewMgr.getNetworkViewSet();
			
			for (var vs : styles) {
				// First register the visual style again
				if (!vmMgr.getAllVisualStyles().contains(vs))
					vmMgr.addVisualStyle(vs);
				
				// Now set the style to the saved views
				for (var view : networkViews) {
					if (viewSet.contains(view)) // Check if this view is still registered
						vmMgr.setVisualStyle(vs, view);
				}
			}
		}

		@Override
		public void redo() {
			if (!styles.isEmpty())
				removeVisualStyles();
		}
	}
}
