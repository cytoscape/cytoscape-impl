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

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class CopyVisualStyleTask extends AbstractTask {

	@Tunable(description = "Name of copied Style:")
	public String vsName;

	private VisualStyle copiedStyle;
	private VisualStyle previousCurrentStyle;
	private final VisualStyle originalStyle;
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public CopyVisualStyleTask(final VisualStyle style, final ServicesUtil servicesUtil) {
		this.originalStyle = style;
		this.servicesUtil = servicesUtil;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@ProvidesTitle
	public String getTitle() {
		return "Copy Style";
	}
	
	@Override
	public void run(final TaskMonitor monitor) throws Exception {
		if (vsName != null && originalStyle != null) {
			copyVisualStyle();
			
			final UndoSupport undo = servicesUtil.get(UndoSupport.class);
			undo.postEdit(new CopyVisualStyleEdit());
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void copyVisualStyle() {
		final VisualStyleFactory vsFactory = servicesUtil.get(VisualStyleFactory.class);
		copiedStyle = vsFactory.createVisualStyle(originalStyle);
		copiedStyle.setTitle(vsName);

		// Save the current visual style first, so it can be set as current again if the action is undone
		final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
		previousCurrentStyle = vmMgr.getCurrentVisualStyle();
		
		vmMgr.addVisualStyle(copiedStyle);
		vmMgr.setCurrentVisualStyle(copiedStyle);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class CopyVisualStyleEdit extends AbstractCyEdit {

		public CopyVisualStyleEdit() {
			super(getTitle());
		}

		@Override
		public void undo() {
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
			
			if (copiedStyle != null && vmMgr.getAllVisualStyles().contains(copiedStyle)) {
				// Unregister the newly created visual style
				vmMgr.removeVisualStyle(copiedStyle);
				
				// Also restore the previous current style
				if (previousCurrentStyle != null)
					vmMgr.setCurrentVisualStyle(previousCurrentStyle);
			}
		}

		@Override
		public void redo() {
			// Register the copied visual style again
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
			
			if (copiedStyle != null && !vmMgr.getAllVisualStyles().contains(copiedStyle)) {
				vmMgr.addVisualStyle(copiedStyle);
				vmMgr.setCurrentVisualStyle(copiedStyle);
			}
		}
	}
}
