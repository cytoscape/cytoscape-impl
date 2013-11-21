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

import java.io.IOException;

import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

public class CreateNewVisualStyleTask extends AbstractTask implements TunableValidator {

	@Tunable(description = "Name of new Style:")
	public String vsName;

	private VisualStyle newStyle;
	private VisualStyle previousCurrentStyle;
	private final ServicesUtil servicesUtil;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public CreateNewVisualStyleTask(final ServicesUtil servicesUtil) {
		this.servicesUtil = servicesUtil;
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@ProvidesTitle
	public String getTitle() {
		return "Create New Style";
	}
	
	@Override
	public void run(final TaskMonitor tm) {
		if (vsName != null) {
			createVisualStyle();
			
			final UndoSupport undo = servicesUtil.get(UndoSupport.class);
			undo.postEdit(new CreateNewVisualStyleEdit());
		}
	}

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);

		for  (final VisualStyle vs : vmMgr.getAllVisualStyles()) {
			if (vs.getTitle().equalsIgnoreCase(vsName)) {
				try {
					errMsg.append("Visual style " + vsName + " already existed.");
					return ValidationState.INVALID;
				} catch (IOException e) {
				}
			}
		}

		return ValidationState.OK;
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================
	
	private void createVisualStyle() {
		final VisualStyleFactory vsFactory = servicesUtil.get(VisualStyleFactory.class);
		newStyle = vsFactory.createVisualStyle(vsName);

		final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
		
		// Save the current visual style first, so it can be set as current again if the action is undone
		previousCurrentStyle = vmMgr.getCurrentVisualStyle();
		
		vmMgr.addVisualStyle(newStyle);
		vmMgr.setCurrentVisualStyle(newStyle);
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class CreateNewVisualStyleEdit extends AbstractCyEdit {

		public CreateNewVisualStyleEdit() {
			super(getTitle());
		}

		@Override
		public void undo() {
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
			
			if (newStyle != null && vmMgr.getAllVisualStyles().contains(newStyle)) {
				// Unregister the newly created visual style
				vmMgr.removeVisualStyle(newStyle);
				
				// Also restore the previous current style
				if (previousCurrentStyle != null)
					vmMgr.setCurrentVisualStyle(previousCurrentStyle);
			}
		}

		@Override
		public void redo() {
			// Register the new visual style again
			final VisualMappingManager vmMgr = servicesUtil.get(VisualMappingManager.class);
			
			if (newStyle != null && !vmMgr.getAllVisualStyles().contains(newStyle)) {
				vmMgr.addVisualStyle(newStyle);
				vmMgr.setCurrentVisualStyle(newStyle);
			}
		}
	}
}